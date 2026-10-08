package org.dev.eclipse.config;

import org.dev.eclipse.websocket.ChatbotWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Value("${app.frontend.url}")
    private String frontendOrigin;

    private final ChatbotWebSocketHandler chatbotWebSocketHandler;

    public WebSocketConfig(ChatbotWebSocketHandler chatbotWebSocketHandler) {
        this.chatbotWebSocketHandler = chatbotWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatbotWebSocketHandler, "/ws/chatbot")
                .setAllowedOrigins(frontendOrigin);
    }
}
