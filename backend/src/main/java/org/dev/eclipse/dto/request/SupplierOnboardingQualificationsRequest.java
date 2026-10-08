package org.dev.eclipse.dto.request;

import java.util.List;

public record SupplierOnboardingQualificationsRequest(
        List<String> certifications,
        List<String> referenceProjects,
        List<String> serviceAreas
) {
}
