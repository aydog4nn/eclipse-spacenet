package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.NotNull;
import org.dev.eclipse.enumarate.Role;

public record ChangeUserRoleRequest(
        @NotNull(message = "Role cannot be null")
        Role role
) {
}
