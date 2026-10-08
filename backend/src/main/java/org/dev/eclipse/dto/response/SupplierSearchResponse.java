package org.dev.eclipse.dto.response;

import org.dev.eclipse.enumarate.SupplierCategory;

import java.util.List;
import java.util.UUID;

public record SupplierSearchResponse(
        UUID organizationId,
        String organizationName,
        SupplierCategory supplierCategory,
        String description,
        String location,
        List<String> expertiseAreas,
        List<String> certifications,
        boolean verified,
        int teamSize,
        double matchScore,
        List<String> matchedReasons
) {
}
