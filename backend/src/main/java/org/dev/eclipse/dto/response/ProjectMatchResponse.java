package org.dev.eclipse.dto.response;

import java.util.List;

public record ProjectMatchResponse(
        ProjectResponse project,
        double matchScore,
        List<String> matchedReasons
) {
}
