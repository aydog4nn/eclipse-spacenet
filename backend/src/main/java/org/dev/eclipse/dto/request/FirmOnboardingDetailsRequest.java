package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record FirmOnboardingDetailsRequest(
        @NotEmpty(message = "En az bir odak alani secilmelidir.")
        List<String> focusAreas,
        List<String> certifications,
        List<String> referenceProjects
) {
}
