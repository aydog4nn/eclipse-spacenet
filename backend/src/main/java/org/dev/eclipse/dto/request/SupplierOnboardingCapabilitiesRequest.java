package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SupplierOnboardingCapabilitiesRequest(
        @NotEmpty(message = "En az bir odak alani secilmelidir.")
        List<String> focusAreas,
        List<String> expertiseAreas,
        List<String> hardwareCapabilities,
        List<String> softwareCapabilities,
        List<String> rdCapabilities,
        List<String> productionInfrastructure,
        List<String> testInfrastructure
) {
}
