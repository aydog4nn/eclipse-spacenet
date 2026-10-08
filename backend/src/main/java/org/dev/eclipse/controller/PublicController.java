package org.dev.eclipse.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dev.eclipse.dto.request.ContactFormRequest;
import org.dev.eclipse.dto.response.AnnouncementResponse;
import org.dev.eclipse.dto.response.RestResponse;
import org.dev.eclipse.service.AnnouncementService;
import org.dev.eclipse.service.MailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public API", description = "Kullaniciya acik endpoint'ler")
public class PublicController {

    private final AnnouncementService announcementService;
    private final MailService mailService;

    public PublicController(AnnouncementService announcementService,
                            MailService mailService) {
        this.announcementService = announcementService;
        this.mailService = mailService;
    }

    @Operation(summary = "Get latest 5 announcements", description = "Yuklenme tarihine gore en son 5 duyuruyu getirir.")
    @ApiResponse(responseCode = "200", description = "Son 5 duyuru basariyla getirildi")
    @GetMapping("/announcements/latest")
    public ResponseEntity<RestResponse<List<AnnouncementResponse>>> getLatestAnnouncements() {
        List<AnnouncementResponse> latest = announcementService.getLatestFiveAnnouncements();
        return ResponseEntity.ok(RestResponse.success("Son 5 duyuru basariyla getirildi", latest));
    }

    @Operation(summary = "Get public announcement detail", description = "Belirli bir duyurunun detaylarini getirir.")
    @ApiResponse(responseCode = "200", description = "Duyuru detayi basariyla getirildi")
    @GetMapping("/announcements/{announcementId}")
    public ResponseEntity<RestResponse<AnnouncementResponse>> getAnnouncementDetail(
            @PathVariable UUID announcementId
    ) {
        AnnouncementResponse announcement = announcementService.getAnnouncementById(announcementId);
        return ResponseEntity.ok(RestResponse.success("Duyuru detayi basariyla getirildi", announcement));
    }


    @Operation(summary = "Send contact form", description = "Iletisim formundan gelen mesaji topluluk e-posta adresine iletir.")
    @ApiResponse(responseCode = "200", description = "Mesaj basariyla gonderildi")
    @PostMapping("/contact")
    public ResponseEntity<RestResponse<Void>> postContactForm(@Valid @RequestBody ContactFormRequest request) {
        mailService.sendContactFormEmail(request.name(), request.email(), request.message());
        return ResponseEntity.ok(RestResponse.success("Mesajiniz basariyla iletildi.", null));
    }
}
