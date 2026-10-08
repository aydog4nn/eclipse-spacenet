package org.dev.eclipse.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket session lifecycle event listener.
 *
 * Neden event listener?
 * → Bağlantı/kopma loglaması, monitoring, metrik toplama
 * → İleride online kullanıcı listesi, typing indicator gibi feature'lar için temel
 * → Debug sırasında hangi kullanıcının ne zaman bağlandığını/koptuğunu izleme
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String user = (accessor.getUser() != null) ? accessor.getUser().getName() : "anonim";

        logger.info("WebSocket bağlantı kuruldu: sessionId={}, user={}", sessionId, user);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String user = (accessor.getUser() != null) ? accessor.getUser().getName() : "anonim";

        logger.info("WebSocket bağlantı koptu: sessionId={}, user={}", sessionId, user);
    }
}
