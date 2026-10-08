package org.dev.eclipse.service;

import org.dev.eclipse.dto.response.FileResourceResponse;
import org.springframework.http.HttpHeaders;

public interface FileService {

    FileResourceResponse getFileResource(String requestPath);

    HttpHeaders createFileHeaders(String fileName, String contentType, long fileLength);

    String determineContentType(String fileName);
}

