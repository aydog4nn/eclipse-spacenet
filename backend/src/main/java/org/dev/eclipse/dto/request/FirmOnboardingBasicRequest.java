package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FirmOnboardingBasicRequest(
        @NotBlank(message = "Kurum adi bos olamaz.")
        String organizationName,
        String description,
        String activityArea,
        String location,
        String website,
        String phoneNumber,
        @Email(message = "Gecerli bir iletisim emaili giriniz.")
        String contactEmail
) {
}
