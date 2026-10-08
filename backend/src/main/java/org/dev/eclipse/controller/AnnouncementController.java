package org.dev.eclipse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.dev.eclipse.dto.request.AnnouncementRequest;
import org.dev.eclipse.dto.response.AnnouncementResponse;
import org.dev.eclipse.dto.response.RestResponse;
import org.dev.eclipse.service.AnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/announcements")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Announcements", description = "Duyuru yönetim API'leri (görsel, başlık, alt başlık, içerik, hashtag)")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @Operation(summary = "Get all announcements", description = "Tüm duyuruları getirir.")
    @ApiResponse(responseCode = "200", description = "Duyurular başarıyla listelendi")
    @GetMapping
    public ResponseEntity<RestResponse<List<AnnouncementResponse>>> getAll() {
        List<AnnouncementResponse> announcements = announcementService.getAllAnnouncements();
        return ResponseEntity.ok(RestResponse.success("Tüm duyurular listelendi", announcements));
    }

    @Operation(summary = "Get announcement by ID", description = "Belirli bir ID’ye sahip duyuruyu getirir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Duyuru bulundu"),
            @ApiResponse(responseCode = "404", description = "Duyuru bulunamadı")
    })
    @GetMapping("/{announcementId}")
    public ResponseEntity<RestResponse<AnnouncementResponse>> getById(
            @Parameter(description = "Duyuru ID", example = "1") @PathVariable UUID announcementId) {
        AnnouncementResponse response = announcementService.getAnnouncementById(announcementId);
        return ResponseEntity.ok(RestResponse.success("Duyuru başarıyla getirildi", response));
    }

    @Operation(summary = "Create new announcement", description = "Yeni bir duyuru oluşturur (tek görsel destekli).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Duyuru başarıyla oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek")
    })
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<RestResponse<AnnouncementResponse>> createAnnouncement(
            @Parameter(description = "Duyuru JSON verisi", required = true)
            @Valid @RequestPart("request") AnnouncementRequest request,

            @Parameter(description = "Duyuru görseli (isteğe bağlı)")
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        AnnouncementResponse created = announcementService.createAnnouncement(request, file);
        return ResponseEntity.ok(RestResponse.success("Duyuru başarıyla oluşturuldu", created));
    }

    @Operation(summary = "Update announcement", description = "Var olan duyuruyu günceller (tek görsel destekli).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Duyuru başarıyla güncellendi"),
            @ApiResponse(responseCode = "404", description = "Duyuru bulunamadı")
    })
    @PutMapping(value = "/{announcementId}", consumes = {"multipart/form-data"})
    public ResponseEntity<RestResponse<AnnouncementResponse>> updateAnnouncement(
            @Parameter(description = "Duyuru ID", example = "1") @PathVariable UUID announcementId,
            @Parameter(description = "Güncellenecek duyuru JSON verisi") @Valid @RequestPart("request") AnnouncementRequest request,
            @Parameter(description = "Yeni görsel (isteğe bağlı)") @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        AnnouncementResponse updated = announcementService.updateAnnouncement(announcementId, request, file);
        return ResponseEntity.ok(RestResponse.success("Duyuru başarıyla güncellendi", updated));
    }

    @Operation(summary = "Delete announcement", description = "Belirli bir ID’ye sahip duyuruyu siler.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Duyuru başarıyla silindi"),
            @ApiResponse(responseCode = "404", description = "Duyuru bulunamadı")
    })
    @DeleteMapping("/{announcementId}")
    public ResponseEntity<Void> deleteAnnouncement(
            @Parameter(description = "Silinecek duyurunun ID'si", example = "1") @PathVariable UUID announcementId) {
        announcementService.deleteAnnouncement(announcementId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search announcements by title", description = "Başlık bazlı duyuru araması yapar.")
    @ApiResponse(responseCode = "200", description = "Arama sonuçları listelendi")
    @GetMapping("/search/title")
    public ResponseEntity<RestResponse<List<AnnouncementResponse>>> searchByTitle(
            @Parameter(description = "Aranacak başlık", example = "Teknofest") @RequestParam String title) {
        List<AnnouncementResponse> results = announcementService.searchByTitle(title);
        return ResponseEntity.ok(RestResponse.success("Başlığa göre arama sonuçları listelendi", results));
    }

    @Operation(summary = "Search announcements by hashtag", description = "Hashtag bazlı duyuru araması yapar.")
    @ApiResponse(responseCode = "200", description = "Arama sonuçları listelendi")
    @GetMapping("/search/hashtag")
    public ResponseEntity<RestResponse<List<AnnouncementResponse>>> searchByHashtag(
            @Parameter(description = "Aranacak hashtag", example = "teknofest") @RequestParam String hashtag) {
        List<AnnouncementResponse> results = announcementService.searchByHashtag(hashtag);
        return ResponseEntity.ok(RestResponse.success("Hashtag'e göre arama sonuçları listelendi", results));
    }
}
