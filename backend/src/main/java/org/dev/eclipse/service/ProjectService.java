package org.dev.eclipse.service;

import org.dev.eclipse.dto.request.ProjectRequest;
import org.dev.eclipse.dto.response.ProjectResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    ProjectResponse createProject(ProjectRequest request);

    ProjectResponse updateProject(UUID projectId, ProjectRequest request);

    void closeProject(UUID projectId);

    List<ProjectResponse> getMyProjects();

    List<ProjectResponse> getActiveProjects();

    ProjectResponse getProjectById(UUID projectId);
}
