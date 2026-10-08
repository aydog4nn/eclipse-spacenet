package org.dev.eclipse.controller;

import jakarta.validation.Valid;
import org.dev.eclipse.dto.request.ApplicationRequest;
import org.dev.eclipse.dto.request.ApplicationStatusUpdateRequest;
import org.dev.eclipse.dto.response.ApplicationResponse;
import org.dev.eclipse.dto.response.RestResponse;
import org.dev.eclipse.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/projects/{projectId}")
    public ResponseEntity<RestResponse<ApplicationResponse>> applyToProject(@PathVariable UUID projectId,
                                                                            @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(RestResponse.success("Application submitted", applicationService.applyToProject(projectId, request)));
    }

    @PatchMapping("/{applicationId}/withdraw")
    public ResponseEntity<RestResponse<String>> withdrawApplication(@PathVariable UUID applicationId) {
        applicationService.withdrawApplication(applicationId);
        return ResponseEntity.ok(RestResponse.success("Application withdrawn successfully"));
    }

    @GetMapping("/my")
    public ResponseEntity<RestResponse<List<ApplicationResponse>>> getMyApplications() {
        return ResponseEntity.ok(RestResponse.success("Applications listed", applicationService.getMyApplications()));
    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<RestResponse<List<ApplicationResponse>>> getApplicationsForProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(RestResponse.success("Project applications listed", applicationService.getApplicationsForProject(projectId)));
    }

    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<RestResponse<ApplicationResponse>> updateApplicationStatus(@PathVariable UUID applicationId,
                                                                                     @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        return ResponseEntity.ok(RestResponse.success("Application status updated", applicationService.updateApplicationStatus(applicationId, request)));
    }
}
