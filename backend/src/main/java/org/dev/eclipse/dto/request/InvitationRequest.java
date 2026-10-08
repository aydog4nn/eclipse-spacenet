package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InvitationRequest(
        @NotNull(message = "Tedarikci kurum id zorunludur.")
        UUID supplierOrganizationId,
        String message
) {
}
