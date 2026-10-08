package org.dev.eclipse.dto.response;

import org.dev.eclipse.enumarate.SenderType;
import org.dev.eclipse.enumarate.StaticMessageCode;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Hem WebSocket broadcast hem de REST mesaj geçmişi için kullanılan response DTO'su.
 *
 * Neden record?
 * → Immutable, toString/equals/hashCode otomatik, serialization dostu.
 * → Jackson ile sorunsuz serialize edilir.
 */
public record ChatMessageResponse(
        UUID messageId,
        UUID projectId,
        SenderType senderType,
        String senderOrganizationName,
        String content,
        StaticMessageCode staticMessageCode,
        LocalDateTime sentAt
) {
}
