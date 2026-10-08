package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ApplicationRequest(
        @NotBlank(message = "Kisa aciklama zorunludur.")
        String shortDescription,
        @NotBlank(message = "Neden uygun oldugunuzu aciklamalisiniz.")
        String whyFit,
        String documentUrl,
        String referenceProjectNote
) {
}
