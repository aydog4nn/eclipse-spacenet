package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Firma (Company) tarafından gönderilen free-text mesaj payload'ı.
 * WebSocket STOMP üzerinden /app/chat.sendCompany'ye gönderilir.
 */
public record CompanyMessageRequest(

        @NotNull(message = "Proje ID boş olamaz")
        UUID projectId,

        @NotBlank(message = "Mesaj içeriği boş olamaz")
        @Size(max = 2000, message = "Mesaj en fazla 2000 karakter olabilir")
        String content
) {
}
