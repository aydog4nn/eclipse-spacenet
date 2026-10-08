package org.dev.eclipse.service;

import java.util.List;

public interface GeminiChatService {

    String generateReply(List<ChatMessage> history);

    record ChatMessage(String role, String text) {
    }
}
