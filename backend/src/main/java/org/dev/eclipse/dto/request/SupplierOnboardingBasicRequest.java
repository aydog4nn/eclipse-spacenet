package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.dev.eclipse.enumarate.SupplierCategory;

public record SupplierOnboardingBasicRequest(
        @NotBlank(message = "Kurum adi bos olamaz.")
        String organizationName,
        @NotNull(message = "Tedarikci alt kategorisi zorunludur.")
        SupplierCategory supplierCategory,
        String description,
        String location,
        String website,
        String phoneNumber,
        @Email(message = "Gecerli bir iletisim emaili giriniz.")
        String contactEmail,
        @PositiveOrZero(message = "Ekip buyuklugu negatif olamaz.")
        Integer teamSize
) {
}
