package org.dev.eclipse.dto.response;

import org.dev.eclipse.enumarate.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationResponse(
        UUID id,
        UUID projectId,
        String projectTitle,
        UUID supplierOrganizationId,
        String supplierOrganizationName,
        String shortDescription,
        String whyFit,
        String documentUrl,
        String referenceProjectNote,
        ApplicationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
