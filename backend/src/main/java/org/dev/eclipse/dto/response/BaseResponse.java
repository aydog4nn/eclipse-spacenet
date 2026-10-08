package org.dev.eclipse.dto.response;

public record BaseResponse(
        boolean success,
        String message
) {}
