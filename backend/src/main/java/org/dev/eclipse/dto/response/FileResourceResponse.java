package org.dev.eclipse.dto.response;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

public record FileResourceResponse(
        Resource resource,
        HttpHeaders headers
) { }