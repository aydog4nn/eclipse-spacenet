package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.dev.eclipse.validation.ValidPassword;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password cannot be blank")
        String currentPassword,

        @NotBlank(message = "Yeni şifre boş olamaz")
        @ValidPassword
        String newPassword,

        @NotBlank(message = "Confirm password cannot be blank")
        @ValidPassword
        String confirmPassword
) {
}
