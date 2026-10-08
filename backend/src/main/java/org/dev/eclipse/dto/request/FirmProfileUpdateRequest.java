package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record FirmProfileUpdateRequest(
        @NotBlank(message = "Kurum adi bos olamaz.")
        String organizationName,
        String description,
        String logoUrl,
        String activityArea,
        String location,
        String website,
        String phoneNumber,
        @Email(message = "Gecerli bir iletisim emaili giriniz.")
        String contactEmail,
        List<String> certifications,
        List<String> referenceProjects,
        List<String> focusAreas,
        Boolean visible
) {
}
