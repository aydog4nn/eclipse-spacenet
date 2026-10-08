package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.NotNull;
import org.dev.eclipse.enumarate.ApplicationStatus;

public record ApplicationStatusUpdateRequest(
        @NotNull(message = "Basvuru durumu zorunludur.")
        ApplicationStatus status
) {
}
