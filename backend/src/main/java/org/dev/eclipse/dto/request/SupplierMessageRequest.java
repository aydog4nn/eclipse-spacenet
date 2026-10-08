package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.NotNull;
import org.dev.eclipse.enumarate.StaticMessageCode;

import java.util.UUID;

/**
 * Tedarikçi (Supplier) tarafından gönderilen statik mesaj payload'ı.
 * WebSocket STOMP üzerinden /app/chat.sendSupplier'a gönderilir.
 *
 * Dikkat: Tedarikçi hiçbir serbest metin göndermez.
 * Sadece önceden tanımlanmış StaticMessageCode enum değerlerinden birini seçer.
 * Mesajın gerçek içeriği (displayText) server tarafında üretilir.
 */
public record SupplierMessageRequest(

        @NotNull(message = "Proje ID boş olamaz")
        UUID projectId,

        @NotNull(message = "Statik mesaj kodu boş olamaz")
        StaticMessageCode staticMessageCode
) {
}
