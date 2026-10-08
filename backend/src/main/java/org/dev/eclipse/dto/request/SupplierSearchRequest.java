package org.dev.eclipse.dto.request;

import org.dev.eclipse.enumarate.SupplierCategory;

public record SupplierSearchRequest(
        String keyword,
        SupplierCategory supplierCategory,
        String location,
        String capability,
        String certification,
        Integer minTeamSize,
        Integer maxTeamSize,
        Boolean verifiedOnly
) {
}
