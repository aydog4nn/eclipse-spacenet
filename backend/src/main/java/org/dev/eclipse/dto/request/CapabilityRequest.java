package org.dev.eclipse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.dev.eclipse.enumarate.CapabilityGroup;

public record CapabilityRequest(
        @NotBlank(message = "Yetkinlik adi bos olamaz.")
        String name,
        @NotNull(message = "Yetkinlik grubu zorunludur.")
        CapabilityGroup capabilityGroup,
        String description,
        Boolean active
) {
}
