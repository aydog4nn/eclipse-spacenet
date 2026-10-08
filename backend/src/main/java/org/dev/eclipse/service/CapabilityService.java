package org.dev.eclipse.service;

import org.dev.eclipse.dto.request.CapabilityRequest;
import org.dev.eclipse.dto.response.CapabilityResponse;

import java.util.List;
import java.util.UUID;

public interface CapabilityService {

    List<CapabilityResponse> getAllActiveCapabilities();

    List<CapabilityResponse> getAllCapabilitiesForAdmin();

    CapabilityResponse createCapability(CapabilityRequest request);

    CapabilityResponse updateCapability(UUID capabilityId, CapabilityRequest request);

    void deleteCapability(UUID capabilityId);
}
