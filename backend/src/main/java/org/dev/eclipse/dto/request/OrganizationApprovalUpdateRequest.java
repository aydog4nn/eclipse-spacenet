package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.NotNull;

public record OrganizationApprovalUpdateRequest(
        @NotNull(message = "Onay durumu zorunludur.")
        Boolean approved
) {
}
