package org.dev.eclipse.dto.response;

import org.dev.eclipse.enumarate.OnboardingStep;
import org.dev.eclipse.enumarate.Role;

public record OnboardingStatusResponse(
        Role role,
        boolean approved,
        boolean onboardingCompleted,
        OnboardingStep onboardingStep,
        int completionPercentage
) {
}
