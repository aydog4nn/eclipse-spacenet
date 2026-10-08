package org.dev.eclipse.websocket;

import io.jsonwebtoken.Claims;
import org.dev.eclipse.entity.User;
import org.dev.eclipse.repository.UserRepository;
import org.dev.eclipse.security.JwtService;
import org.dev.eclipse.service.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * STOMP CONNECT frame'inde JWT doğrulaması yapan ChannelInterceptor.
 *
 * Neden HTTP filter yerine ChannelInterceptor?
 * → WebSocket handshake HTTP üzerinden olur ama STOMP mesajları HTTP filter'dan geçmez.
 * → STOMP CONNECT frame'inde gelen native header'lardan JWT çıkarmak gerekir.
 * → ChannelInterceptor STOMP katmanında çalışır, tam da ihtiyacımız olan yer.
 *
 * Akış:
 * 1. Client STOMP CONNECT gönderir, Authorization header'ında "Bearer xxx" taşır
 * 2. Bu interceptor token'ı parse eder
 * 3. Token geçerliyse → Principal (Authentication) set eder
 * 4. Sonraki tüm SEND/SUBSCRIBE frame'lerinde bu Principal kullanılır
 * 5. Token geçersizse → IllegalArgumentException fırlatır, Spring STOMP ERROR frame gönderir
 *
 * Client tarafı (JavaScript STOMP):
 * ```javascript
 * const client = new Client({
 *   brokerURL: 'ws://localhost:8080/ws/messaging',
 *   connectHeaders: {
 *     Authorization: 'Bearer ' + accessToken
 *   }
 * });
 * ```
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final org.dev.eclipse.service.ChatMessageService chatMessageService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    public WebSocketAuthInterceptor(JwtService jwtService,
                                    UserRepository userRepository,
                                    TokenBlacklistService tokenBlacklistService,
                                    org.dev.eclipse.service.ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // Sadece CONNECT frame'inde JWT doğrulaması yap
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("WebSocket CONNECT: Authorization header eksik veya geçersiz format");
                throw new IllegalArgumentException("Yetkilendirme başlığı gerekli. Format: Bearer <token>");
            }

            String token = authHeader.substring(7);

            // Blacklist kontrolü
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                logger.warn("WebSocket CONNECT: Token blacklisted");
                throw new IllegalArgumentException("Token geçersiz (blacklisted)");
            }

            try {
                // Token doğrulama
                jwtService.validateTokenType(token, "access");
                String email = jwtService.extractEmail(token);
                Claims claims = jwtService.extractAllClaims(token);
                String role = claims.get("role", String.class);

                // Kullanıcıyı DB'den doğrula
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

                if (!user.isActive()) {
                    throw new IllegalArgumentException("Kullanıcı hesabı aktif değil");
                }

                // Principal oluştur ve set et
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),    // principal → email string
                                null,               // credentials
                                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                        );

                accessor.setUser(authentication);
                logger.info("WebSocket CONNECT başarılı: email={}, role={}", email, role);

            } catch (IllegalArgumentException e) {
                throw e; // Yukarıdaki custom hatalar
            } catch (Exception e) {
                logger.error("WebSocket CONNECT JWT doğrulama hatası: {}", e.getMessage());
                throw new IllegalArgumentException("Token doğrulama başarısız");
            }
        }

        if (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            if (accessor.getUser() == null) {
                throw new IllegalArgumentException("Yetkilendirme gerekli");
            }
            String destination = accessor.getDestination();
            if (StompCommand.SEND.equals(accessor.getCommand())) {
                if (!"/app/chat.sendCompany".equals(destination) && !"/app/chat.sendSupplier".equals(destination)) {
                    throw new IllegalArgumentException("Hedefe erişim reddedildi");
                }
            } else if (!"/user/queue/errors".equals(destination)) {
                if (destination == null || !destination.matches("/topic/projects/[0-9a-fA-F-]{36}/chat")) {
                    throw new IllegalArgumentException("Hedefe erişim reddedildi");
                }
                java.util.UUID projectId = java.util.UUID.fromString(destination.split("/")[3]);
                chatMessageService.authorizeProjectAccess(projectId, accessor.getUser().getName());
            }
        }
        return message;
    }
}
