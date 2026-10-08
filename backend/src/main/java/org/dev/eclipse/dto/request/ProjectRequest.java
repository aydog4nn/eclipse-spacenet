package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.dev.eclipse.enumarate.CollaborationType;
import org.dev.eclipse.enumarate.ConfidentialityLevel;
import org.dev.eclipse.enumarate.ProjectStatus;
import org.dev.eclipse.enumarate.SupplierCategory;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectRequest(
        @NotBlank(message = "Ilan basligi zorunludur.")
        String title,
        @NotBlank(message = "Ilan aciklamasi zorunludur.")
        String description,
        @NotBlank(message = "Proje turu zorunludur.")
        String projectType,
        @NotNull(message = "Is birligi tipi zorunludur.")
        CollaborationType collaborationType,
        @NotNull(message = "Aranan yetkinlikler zorunludur.")
        List<UUID> capabilityIds,
        SupplierCategory preferredSupplierCategory,
        String locationPreference,
        String durationText,
        String budgetRange,
        List<String> certificationRequirements,
        @Future(message = "Son basvuru tarihi gelecekte olmalidir.")
        LocalDate applicationDeadline,
        @NotNull(message = "Gizlilik seviyesi zorunludur.")
        ConfidentialityLevel confidentialityLevel,
        @NotNull(message = "Ilan durumu zorunludur.")
        ProjectStatus status
) {
}
