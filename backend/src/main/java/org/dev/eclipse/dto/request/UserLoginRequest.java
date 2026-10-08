package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @Email(message = "Geçerli bir email giriniz.")
        String email,

        @NotBlank(message = "Şifre boş olamaz.")
        String password
) {}

