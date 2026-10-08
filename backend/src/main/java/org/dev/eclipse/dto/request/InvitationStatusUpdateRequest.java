package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.NotNull;
import org.dev.eclipse.enumarate.InvitationStatus;

public record InvitationStatusUpdateRequest(
        @NotNull(message = "Davet durumu zorunludur.")
        InvitationStatus status
) {
}
