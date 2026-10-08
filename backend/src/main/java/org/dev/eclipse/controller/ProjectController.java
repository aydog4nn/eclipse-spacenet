package org.dev.eclipse.controller;

import jakarta.validation.Valid;
import org.dev.eclipse.dto.request.ProjectRequest;
import org.dev.eclipse.dto.response.ProjectResponse;
import org.dev.eclipse.dto.response.RestResponse;
import org.dev.eclipse.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<RestResponse<ProjectResponse>> createProject(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(RestResponse.success("Project created", projectService.createProject(request)));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<RestResponse<ProjectResponse>> updateProject(@PathVariable UUID projectId,
                                                                       @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(RestResponse.success("Project updated", projectService.updateProject(projectId, request)));
    }

    @PatchMapping("/{projectId}/close")
    public ResponseEntity<RestResponse<String>> closeProject(@PathVariable UUID projectId) {
        projectService.closeProject(projectId);
        return ResponseEntity.ok(RestResponse.success("Project closed successfully"));
    }

    @GetMapping
    public ResponseEntity<RestResponse<List<ProjectResponse>>> getAllProjects() {
        return ResponseEntity.ok(RestResponse.success("Active projects listed", projectService.getActiveProjects()));
    }

    @GetMapping("/my")
    public ResponseEntity<RestResponse<List<ProjectResponse>>> getMyProjects() {
        return ResponseEntity.ok(RestResponse.success("Projects listed", projectService.getMyProjects()));
    }

    @GetMapping("/active")
    public ResponseEntity<RestResponse<List<ProjectResponse>>> getActiveProjects() {
        return ResponseEntity.ok(RestResponse.success("Active projects listed", projectService.getActiveProjects()));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<RestResponse<ProjectResponse>> getProjectById(@PathVariable UUID projectId) {
        return ResponseEntity.ok(RestResponse.success("Project retrieved", projectService.getProjectById(projectId)));
    }
}
