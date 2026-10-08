package org.dev.eclipse.controller;

import org.dev.eclipse.dto.request.SupplierSearchRequest;
import org.dev.eclipse.dto.response.ProjectMatchResponse;
import org.dev.eclipse.dto.response.RestResponse;
import org.dev.eclipse.dto.response.SupplierSearchResponse;
import org.dev.eclipse.service.OrganizationSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/discovery")
public class DiscoveryController {

    private final OrganizationSearchService organizationSearchService;

    public DiscoveryController(OrganizationSearchService organizationSearchService) {
        this.organizationSearchService = organizationSearchService;
    }

    @GetMapping("/suppliers")
    public ResponseEntity<RestResponse<List<SupplierSearchResponse>>> searchSuppliers(@ModelAttribute SupplierSearchRequest request) {
        return ResponseEntity.ok(RestResponse.success("Suppliers listed", organizationSearchService.searchSuppliers(request)));
    }

    @GetMapping("/projects/{projectId}/supplier-matches")
    public ResponseEntity<RestResponse<List<SupplierSearchResponse>>> getSupplierMatchesForProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(RestResponse.success("Supplier matches listed", organizationSearchService.getSupplierMatchesForProject(projectId)));
    }

    @GetMapping("/me/matching-projects")
    public ResponseEntity<RestResponse<List<ProjectMatchResponse>>> getMatchingProjectsForCurrentSupplier() {
        return ResponseEntity.ok(RestResponse.success("Matching projects listed", organizationSearchService.getMatchingProjectsForCurrentSupplier()));
    }
}
