package org.dev.eclipse.dto.response;

import org.dev.eclipse.enumarate.SupplierCategory;

import java.util.List;

public record WebsiteSuggestionResponse(
        String website,
        SupplierCategory suggestedSupplierCategory,
        List<String> suggestedFocusAreas,
        List<String> suggestedHardwareCapabilities,
        List<String> suggestedSoftwareCapabilities,
        List<String> suggestedRdCapabilities,
        List<String> suggestedServiceAreas,
        String summary
) {
}
