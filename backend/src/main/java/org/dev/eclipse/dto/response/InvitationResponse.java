package org.dev.eclipse.dto.response;

import org.dev.eclipse.enumarate.InvitationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record InvitationResponse(
        UUID id,
        UUID projectId,
        String projectTitle,
        UUID supplierOrganizationId,
        String supplierOrganizationName,
        String message,
        InvitationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
