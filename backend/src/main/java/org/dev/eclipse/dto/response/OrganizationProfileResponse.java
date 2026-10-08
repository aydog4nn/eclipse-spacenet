package org.dev.eclipse.dto.response;

import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.enumarate.SupplierCategory;
import org.dev.eclipse.enumarate.OnboardingStep;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrganizationProfileResponse(
        UUID userId,
        String email,
        String username,
        UUID organizationId,
        String organizationName,
        Role role,
        SupplierCategory supplierCategory,
        String description,
        String logoUrl,
        String activityArea,
        String location,
        String website,
        String phoneNumber,
        String contactEmail,
        Integer teamSize,
        List<String> focusAreas,
        List<String> certifications,
        List<String> referenceProjects,
        List<String> certificateFileUrls,
        List<String> certificateFileLabels,
        List<String> expertiseAreas,
        List<String> hardwareCapabilities,
        List<String> softwareCapabilities,
        List<String> rdCapabilities,
        List<String> productionInfrastructure,
        List<String> testInfrastructure,
        List<String> serviceAreas,
        boolean verified,
        boolean approved,
        boolean onboardingCompleted,
        OnboardingStep onboardingStep,
        boolean visible,
        boolean active,
        int completionPercentage,
        LocalDateTime lastLoginAt
) {
}
