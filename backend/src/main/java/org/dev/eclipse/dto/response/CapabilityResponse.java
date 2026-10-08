package org.dev.eclipse.dto.response;

import org.dev.eclipse.enumarate.CapabilityGroup;

import java.util.UUID;

public record CapabilityResponse(
        UUID id,
        String name,
        CapabilityGroup capabilityGroup,
        String description,
        boolean active
) {
}
