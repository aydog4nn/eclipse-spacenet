package org.dev.eclipse.websocket;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.dev.eclipse.dto.request.ChatbotMessageRequest;
import org.dev.eclipse.dto.response.ChatbotMessageResponse;
import org.dev.eclipse.service.GeminiChatService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatbotWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotWebSocketHandler.class);
    private static final int MAX_HISTORY_SIZE = 20;

    private final ObjectMapper objectMapper;
    private final GeminiChatService geminiChatService;
    private final Map<String, List<GeminiChatService.ChatMessage>> sessionHistories = new ConcurrentHashMap<>();

    public ChatbotWebSocketHandler(ObjectMapper objectMapper, GeminiChatService geminiChatService) {
        this.objectMapper = objectMapper;
        this.geminiChatService = geminiChatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        sessionHistories.put(session.getId(), new ArrayList<>());
        sendMessage(session, new ChatbotMessageResponse(
                "system",
                "Chatbot baglantisi hazir. Mesaj gonderebilirsiniz.",
                OffsetDateTime.now().toString()
        ));
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws IOException {
        ChatbotMessageRequest request;
        try {
            request = parseRequest(message.getPayload());
        } catch (JacksonException ex) {
            sendMessage(session, new ChatbotMessageResponse(
                    "error",
                    "Gecersiz mesaj formati. JSON olarak {\"message\":\"...\"} gondermelisiniz.",
                    OffsetDateTime.now().toString()
            ));
            return;
        }

        if (!StringUtils.hasText(request.message())) {
            sendMessage(session, new ChatbotMessageResponse(
                    "error",
                    "Mesaj bos olamaz.",
                    OffsetDateTime.now().toString()
            ));
            return;
        }

        List<GeminiChatService.ChatMessage> history = sessionHistories.computeIfAbsent(session.getId(), key -> new ArrayList<>());
        history.add(new GeminiChatService.ChatMessage("user", request.message().trim()));
        trimHistory(history);

        try {
            String reply = geminiChatService.generateReply(List.copyOf(history));
            history.add(new GeminiChatService.ChatMessage("model", reply));
            trimHistory(history);

            sendMessage(session, new ChatbotMessageResponse(
                    "assistant",
                    reply,
                    OffsetDateTime.now().toString()
            ));
        } catch (Exception ex) {
            logger.error("Chatbot response failed for session {}", session.getId(), ex);
            history.remove(history.size() - 1);
            sendMessage(session, new ChatbotMessageResponse(
                    "error",
                    "Asistan şu anda yanıt veremiyor. Lütfen daha sonra tekrar deneyin.",
                    OffsetDateTime.now().toString()
            ));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
        sessionHistories.remove(session.getId());
    }

    private ChatbotMessageRequest parseRequest(String payload) throws JacksonException {
        return objectMapper.readValue(payload, ChatbotMessageRequest.class);
    }

    private void sendMessage(WebSocketSession session, ChatbotMessageResponse response) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private void trimHistory(List<GeminiChatService.ChatMessage> history) {
        while (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
    }
}
