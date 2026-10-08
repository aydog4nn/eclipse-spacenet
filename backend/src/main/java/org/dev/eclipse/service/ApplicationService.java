package org.dev.eclipse.service;

import org.dev.eclipse.dto.request.ApplicationRequest;
import org.dev.eclipse.dto.request.ApplicationStatusUpdateRequest;
import org.dev.eclipse.dto.response.ApplicationResponse;

import java.util.List;
import java.util.UUID;

public interface ApplicationService {

    ApplicationResponse applyToProject(UUID projectId, ApplicationRequest request);

    void withdrawApplication(UUID applicationId);

    List<ApplicationResponse> getMyApplications();

    List<ApplicationResponse> getApplicationsForProject(UUID projectId);

    ApplicationResponse updateApplicationStatus(UUID applicationId, ApplicationStatusUpdateRequest request);
}
