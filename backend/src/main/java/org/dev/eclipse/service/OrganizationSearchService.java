package org.dev.eclipse.service;

import org.dev.eclipse.dto.request.SupplierSearchRequest;
import org.dev.eclipse.dto.response.ProjectMatchResponse;
import org.dev.eclipse.dto.response.SupplierSearchResponse;
import org.dev.eclipse.entity.Organization;

import java.util.List;
import java.util.UUID;

public interface OrganizationSearchService {

    void indexOrganization(Organization organization);

    void removeOrganization(UUID organizationId);

    List<SupplierSearchResponse> searchSuppliers(SupplierSearchRequest request);

    List<SupplierSearchResponse> getSupplierMatchesForProject(UUID projectId);

    List<ProjectMatchResponse> getMatchingProjectsForCurrentSupplier();
}
