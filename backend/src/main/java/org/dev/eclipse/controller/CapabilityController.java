package org.dev.eclipse.controller;

import jakarta.validation.Valid;
import org.dev.eclipse.dto.request.CapabilityRequest;
import org.dev.eclipse.dto.response.CapabilityResponse;
import org.dev.eclipse.dto.response.RestResponse;
import org.dev.eclipse.service.CapabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/capabilities")
public class CapabilityController {

    private final CapabilityService capabilityService;

    public CapabilityController(CapabilityService capabilityService) {
        this.capabilityService = capabilityService;
    }

    @GetMapping
    public ResponseEntity<RestResponse<List<CapabilityResponse>>> getActiveCapabilities() {
        return ResponseEntity.ok(RestResponse.success("Active capabilities listed", capabilityService.getAllActiveCapabilities()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<RestResponse<List<CapabilityResponse>>> getAllCapabilitiesForAdmin() {
        return ResponseEntity.ok(RestResponse.success("All capabilities listed", capabilityService.getAllCapabilitiesForAdmin()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<RestResponse<CapabilityResponse>> createCapability(@Valid @RequestBody CapabilityRequest request) {
        return ResponseEntity.ok(RestResponse.success("Capability created", capabilityService.createCapability(request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{capabilityId}")
    public ResponseEntity<RestResponse<CapabilityResponse>> updateCapability(@PathVariable UUID capabilityId,
                                                                             @Valid @RequestBody CapabilityRequest request) {
        return ResponseEntity.ok(RestResponse.success("Capability updated", capabilityService.updateCapability(capabilityId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{capabilityId}")
    public ResponseEntity<Void> deleteCapability(@PathVariable UUID capabilityId) {
        capabilityService.deleteCapability(capabilityId);
        return ResponseEntity.noContent().build();
    }
}
