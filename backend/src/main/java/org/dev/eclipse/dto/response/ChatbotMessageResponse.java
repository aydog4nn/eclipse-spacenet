package org.dev.eclipse.dto.response;

public record ChatbotMessageResponse(
        String type,
        String message,
        String timestamp
) {
}
