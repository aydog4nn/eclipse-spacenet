package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WebsiteSuggestionRequest(
        @NotBlank(message = "Website bos olamaz.")
        String website
) {
}
