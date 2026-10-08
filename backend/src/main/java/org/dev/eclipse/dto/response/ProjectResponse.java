package org.dev.eclipse.dto.response;

import org.dev.eclipse.enumarate.CollaborationType;
import org.dev.eclipse.enumarate.ConfidentialityLevel;
import org.dev.eclipse.enumarate.ProjectStatus;
import org.dev.eclipse.enumarate.SupplierCategory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID organizationId,
        String organizationName,
        String title,
        String description,
        String projectType,
        CollaborationType collaborationType,
        List<CapabilityResponse> requiredCapabilities,
        SupplierCategory preferredSupplierCategory,
        String locationPreference,
        String durationText,
        String budgetRange,
        List<String> certificationRequirements,
        LocalDate applicationDeadline,
        ConfidentialityLevel confidentialityLevel,
        ProjectStatus status,
        LocalDateTime createdAt
) {
}
