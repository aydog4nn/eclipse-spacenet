package org.dev.eclipse.dto.response;

import java.time.LocalDateTime;

/**
 * WebSocket üzerinden /user/queue/errors kanalına gönderilen hata response'u.
 * STOMP ERROR frame'i yerine daha zengin hata bilgisi sunmak için kullanılır.
 */
public record ChatErrorResponse(
        String error,
        String message,
        LocalDateTime timestamp
) {
    public static ChatErrorResponse of(String error, String message) {
        return new ChatErrorResponse(error, message, LocalDateTime.now());
    }
}
