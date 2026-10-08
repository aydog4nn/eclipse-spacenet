package org.dev.eclipse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.dev.eclipse.dto.response.FileResourceResponse;
import org.dev.eclipse.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/files", "/uploads"})
@Tag(name = "File Service", description = "Dosya görüntüleme ve indirme API'leri")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(
            summary = "Dosya görüntüleme veya indirme",
            description = "Uploads dizinindeki dosyaları tarayıcıda görüntüler veya indirir. " +
                    "Veritabanında kayıtlı dosya yollarına göre erişim sağlar."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dosya başarıyla servis edildi",
                    content = @Content(schema = @Schema(implementation = Resource.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz dosya yolu"),
            @ApiResponse(responseCode = "403", description = "Erişim reddedildi"),
            @ApiResponse(responseCode = "404", description = "Dosya bulunamadı"),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    @GetMapping("/**")
    public ResponseEntity<Resource> serveFile(
            @Parameter(description = "Veritabanında saklanan tam dosya yolu (örnek: uploads/events/file.pdf)")
            HttpServletRequest request) {

        String requestURI = request.getRequestURI();
        String requestPath = requestURI
                .replaceFirst("^/api/v1/files/?", "")
                .replaceFirst("^/uploads/?", "");

        if (requestPath.isEmpty()) {
            throw new IllegalArgumentException("Dosya yolu belirtilmemiş.");
        }

        if (requestPath.startsWith("uploads/")) {
            requestPath = requestPath.substring("uploads/".length());
        }

        FileResourceResponse fileResponse = fileService.getFileResource(requestPath);

        return ResponseEntity.ok()
                .headers(fileResponse.headers())
                .body(fileResponse.resource());
    }
}
