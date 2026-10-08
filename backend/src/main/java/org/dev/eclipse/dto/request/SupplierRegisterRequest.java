package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.dev.eclipse.enumarate.SupplierCategory;
import org.dev.eclipse.validation.ValidPassword;

import java.util.List;

public record SupplierRegisterRequest(
        @NotBlank(message = "Kurum adi bos olamaz.")
        String organizationName,

        @NotBlank(message = "Kullanici adi bos olamaz.")
        @Size(min = 3, max = 50, message = "Kullanici adi 3 ile 50 karakter arasinda olmalidir.")
        String username,

        @Email(message = "Gecerli bir email giriniz.")
        @NotBlank(message = "Email bos olamaz.")
        String email,

        @NotBlank(message = "Sifre bos olamaz.")
        @ValidPassword
        String password,

        @NotBlank(message = "Telefon numarasi bos olamaz.")
        @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Gecerli bir telefon numarasi giriniz.")
        String phoneNumber,

        @NotNull(message = "Tedarikci alt kategorisi bos olamaz.")
        SupplierCategory supplierCategory,

        String location,
        String website,

        @NotEmpty(message = "En az bir odak alani girilmelidir.")
        List<@NotBlank(message = "Odak alani bos olamaz.") String> focusAreas
) {
}
