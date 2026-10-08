package org.dev.eclipse.dto.response;

public record UploadedFileResponse(
        String filePath,
        String fileUrl,
        String displayName
) {
}
