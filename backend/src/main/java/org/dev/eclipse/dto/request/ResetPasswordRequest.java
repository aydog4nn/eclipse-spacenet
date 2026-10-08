package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.dev.eclipse.validation.ValidPassword;

public record ResetPasswordRequest(
        @NotBlank(message = "Token boş olamaz")
        String token,

        @NotBlank(message = "Yeni şifre boş olamaz")
        @ValidPassword
        String newPassword,

        @NotBlank(message = "Şifre tekrarı boş olamaz")
        @ValidPassword
        String confirmPassword
) {
}

