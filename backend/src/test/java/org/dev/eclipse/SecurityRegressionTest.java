package org.dev.eclipse;

import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.entity.User;
import org.dev.eclipse.exception.AuthExceptions.AuthException;
import org.dev.eclipse.repository.UserRepository;
import org.dev.eclipse.security.JwtService;
import org.dev.eclipse.service.ChatMessageService;
import org.dev.eclipse.service.TokenBlacklistService;
import org.dev.eclipse.websocket.WebSocketAuthInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityRegressionTest {
    private JwtService jwt;
    private UserRepository users;
    private ChatMessageService chat;
    private WebSocketAuthInterceptor interceptor;

    @BeforeEach
    void setup() {
        jwt = new JwtService();
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        ReflectionTestUtils.setField(jwt, "secretKey", Base64.getEncoder().encodeToString(key));
        ReflectionTestUtils.setField(jwt, "accessTokenExpiration", 60_000L);
        ReflectionTestUtils.setField(jwt, "refreshTokenExpiration", 120_000L);
        ReflectionTestUtils.setField(jwt, "csrfTokenExpiration", 120_000L);
        users = mock(UserRepository.class);
        chat = mock(ChatMessageService.class);
        interceptor = new WebSocketAuthInterceptor(jwt, users, mock(TokenBlacklistService.class), chat);
    }

    @Test
    void tokensCannotBeSubstitutedAcrossPurposes() {
        String access = jwt.generateAccessToken("person@example.invalid", Role.FIRMA);
        String refresh = jwt.generateRefreshToken("person@example.invalid");
        String csrf = jwt.generateCsrfToken("person@example.invalid");
        assertDoesNotThrow(() -> jwt.validateTokenType(access, "access"));
        assertDoesNotThrow(() -> jwt.validateTokenType(refresh, "refresh"));
        assertDoesNotThrow(() -> jwt.validateTokenType(csrf, "csrf"));
        assertThrows(AuthException.class, () -> jwt.validateTokenType(refresh, "access"));
        assertThrows(AuthException.class, () -> jwt.validateTokenType(csrf, "access"));
        assertThrows(AuthException.class, () -> jwt.validateTokenType(access, "refresh"));
    }

    @Test
    void invalidJwtDoesNotExposeParserDetails() {
        AuthException error = assertThrows(AuthException.class, () -> jwt.validateToken("invalid"));
        assertEquals("Token geçersiz", error.getMessage());
    }

    @Test
    void anonymousSubscriptionIsRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/projects/" + UUID.randomUUID() + "/chat", false), null));
    }

    @Test
    void subscriptionChecksProjectAuthorization() {
        UUID project = UUID.randomUUID();
        interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/projects/" + project + "/chat", true), null);
        verify(chat).authorizeProjectAccess(project, "person@example.invalid");
        doThrow(new IllegalArgumentException("denied")).when(chat).authorizeProjectAccess(project, "person@example.invalid");
        assertThrows(IllegalArgumentException.class,
            () -> interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/projects/" + project + "/chat", true), null));
    }

    @Test
    void clientCannotSendToBrokerOrSubscribeToWildcard() {
        assertThrows(IllegalArgumentException.class,
            () -> interceptor.preSend(frame(StompCommand.SEND, "/topic/projects/test/chat", true), null));
        assertThrows(IllegalArgumentException.class,
            () -> interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/**", true), null));
    }

    @Test
    void connectRejectsRefreshTokenAndInactiveUser() {
        StompHeaderAccessor refreshHeaders = StompHeaderAccessor.create(StompCommand.CONNECT);
        refreshHeaders.setNativeHeader("Authorization", "Bearer " + jwt.generateRefreshToken("person@example.invalid"));
        assertThrows(IllegalArgumentException.class,
            () -> interceptor.preSend(MessageBuilder.createMessage(new byte[0], refreshHeaders.getMessageHeaders()), null));
        User user = new User();
        user.setEmail("person@example.invalid");
        user.setRole(Role.FIRMA);
        user.setActive(false);
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        StompHeaderAccessor accessHeaders = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessHeaders.setNativeHeader("Authorization", "Bearer " + jwt.generateAccessToken(user.getEmail(), Role.FIRMA));
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessHeaders.getMessageHeaders());
        assertThrows(IllegalArgumentException.class, () -> interceptor.preSend(message, null));
    }

    private Message<byte[]> frame(StompCommand command, String destination, boolean authenticated) {
        StompHeaderAccessor headers = StompHeaderAccessor.create(command);
        headers.setDestination(destination);
        if (authenticated) headers.setUser(new UsernamePasswordAuthenticationToken("person@example.invalid", null, java.util.List.of()));
        return MessageBuilder.createMessage(new byte[0], headers.getMessageHeaders());
    }
}
