package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactFormRequest(
        @NotBlank(message = "Ad alani bos olamaz")
        String name,

        @NotBlank(message = "E-posta alani bos olamaz")
        @Email(message = "Gecerli bir e-posta adresi girin")
        String email,

        @NotBlank(message = "Mesaj alani bos olamaz")
        @Size(max = 4000, message = "Mesaj en fazla 4000 karakter olabilir")
        String message
) {
}
