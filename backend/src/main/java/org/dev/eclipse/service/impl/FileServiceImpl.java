package org.dev.eclipse.service.impl;

import org.dev.eclipse.annotation.Audit;
import org.dev.eclipse.dto.response.FileResourceResponse;
import org.dev.eclipse.exception.FileExceptions.FileNotFoundException;
import org.dev.eclipse.exception.FileExceptions.InvalidFilePathException;
import org.dev.eclipse.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    private static final String UPLOAD_DIR = "uploads";

    @Override
    @Audit(action = "GET_FILE_RESOURCE", resource = "File")
    @Transactional(readOnly = true)
    public FileResourceResponse getFileResource(String requestPath) {
        logger.info("Requested file path: {}", requestPath);

        if (requestPath == null || requestPath.isEmpty()) {
            logger.warn("File path is empty");
            throw new InvalidFilePathException("Dosya yolu boş olamaz.");
        }

        Path uploadDir = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Path filePath = uploadDir.resolve(requestPath).normalize();

        if (!filePath.startsWith(uploadDir)) {
            logger.warn("Unauthorized access attempt outside upload directory: {}", requestPath);
            throw new SecurityException("Erişim reddedildi: Yetkisiz dosya yolu tespiti.");
        }

        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            logger.warn("File not found: {}", filePath);
            throw new FileNotFoundException("Dosya bulunamadı: " + requestPath);
        }

        try {
            if (!filePath.toRealPath().startsWith(uploadDir.toRealPath())) {
                throw new SecurityException("Erişim reddedildi");
            }
        } catch (java.io.IOException ex) {
            throw new FileNotFoundException("Dosya bulunamadı");
        }
        Resource resource = new FileSystemResource(file);
        String contentType = determineContentType(file.getName());
        HttpHeaders headers = createFileHeaders(file.getName(), contentType, file.length());

        logger.debug("File ready for response -> name: {}, type: {}, size: {} bytes",
                file.getName(), contentType, file.length());

        return new FileResourceResponse(resource, headers);
    }

    @Override
    public HttpHeaders createFileHeaders(String fileName, String contentType, long fileLength) {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(fileLength);

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''" + encodedFileName);

        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.set(HttpHeaders.CACHE_CONTROL, "private, no-store");

        logger.trace("Headers created for file: {}", fileName);
        return headers;
    }

    @Override
    public String determineContentType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "application/octet-stream";
        }

        String lower = fileName.toLowerCase();

        return switch (getFileExtension(lower)) {
            case "pdf" -> "application/pdf";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt" -> "text/plain";
            case "json" -> "application/json";
            default -> probeContentTypeOrDefault(fileName);
        };
    }

    private String probeContentTypeOrDefault(String fileName) {
        try {
            Path filePath = Paths.get(fileName);
            String detectedType = Files.probeContentType(filePath);
            if (detectedType != null) return detectedType;
        } catch (Exception e) {
            logger.debug("Unable to detect MIME type for file: {}", fileName, e);
        }
        return "application/octet-stream";
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

}
