package org.dev.eclipse.dto.response;

import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.enumarate.SupplierCategory;
import org.dev.eclipse.enumarate.OnboardingStep;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String username,
        String name,
        String surname,
        UUID organizationId,
        String organizationName,
        String phoneNumber,
        String location,
        String website,
        SupplierCategory supplierCategory,
        List<String> focusAreas,
        boolean verified,
        boolean approved,
        boolean onboardingCompleted,
        OnboardingStep onboardingStep,
        Role role,
        boolean active,
        LocalDateTime lastLoginAt
) {
}
