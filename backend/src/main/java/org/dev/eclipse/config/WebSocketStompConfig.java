package org.dev.eclipse.config;

import org.dev.eclipse.websocket.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP WebSocket yapılandırması — Proje bazlı mesajlaşma sistemi için.
 *
 * ÖNEMLİ: Mevcut WebSocketConfig.java (chatbot raw WebSocket) dokunulmadı.
 * Bu config ayrı bir STOMP message broker kurar. İkisi birlikte çalışır.
 *
 * Neden STOMP?
 * → Raw WebSocket'te topic routing, subscription management, error handling tamamen custom.
 * → STOMP ile /topic, /queue destination'ları, @MessageMapping annotation'ları,
 *   ChannelInterceptor ile JWT auth, user-specific error queue (/user/queue/errors)
 *   hepsi out-of-the-box gelir.
 *
 * Neden SockJS?
 * → WebSocket desteklemeyen proxy/firewall arkasındaki client'lar için HTTP fallback sağlar.
 * → Production ortamında güvenilirlik artırır.
 *
 * Mimari:
 * → /app prefix: Client'ın mesaj gönderdiği endpoint'ler (@MessageMapping)
 * → /topic prefix: Broadcast kanalları (1-to-many, proje bazlı sohbet)
 * → /queue prefix: Point-to-point kanallar (user-specific errors)
 * → /user prefix: Spring STOMP'un user-specific destination çözümlemesi
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketStompConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.frontend.url}")
    private String frontendOrigin;

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketStompConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    /**
     * Message broker yapılandırması.
     *
     * enableSimpleBroker: In-memory broker. /topic → broadcast, /queue → point-to-point
     * setApplicationDestinationPrefixes: Client /app/... 'e mesaj gönderir, @MessageMapping bu prefix'i çıkarıp eşleştirir
     * setUserDestinationPrefix: /user/queue/errors gibi kullanıcıya özel destination'lar için
     *
     * Not: Production'da RabbitMQ/ActiveMQ gibi external broker kullanılabilir.
     * enableStompBrokerRelay() ile geçiş 1 satırlık değişiklik.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * STOMP WebSocket endpoint tanımı.
     *
     * /ws/messaging → Client bu URL'ye WebSocket/SockJS ile bağlanır.
     * withSockJS() → HTTP fallback desteği.
     *
     * Not: Bu endpoint SecurityConfig'de permitAll() olarak eklendi.
     * JWT doğrulaması HTTP seviyesinde değil, STOMP CONNECT frame'inde yapılır.
     * (Bkz: WebSocketAuthInterceptor)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/messaging")
                .setAllowedOrigins(frontendOrigin)
                .withSockJS();
    }

    /**
     * Inbound channel'a interceptor ekleme.
     * WebSocketAuthInterceptor STOMP CONNECT frame'inde JWT doğrulaması yapar
     * ve Principal set eder. Sonraki tüm STOMP operasyonlarında bu Principal kullanılır.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
