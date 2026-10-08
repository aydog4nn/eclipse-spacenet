package org.dev.eclipse.controller;

import org.dev.eclipse.dto.response.ChatMessageResponse;
import org.dev.eclipse.dto.response.RestResponse;
import org.dev.eclipse.service.ChatMessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST endpoint'leri — Mesaj geçmişi ve okunmamış mesaj sayısı.
 *
 * Neden REST ayrı controller?
 * → WebSocket sadece gerçek zamanlı yeni mesajlar için kullanılır.
 * → Mesaj geçmişi (sayfalı), okunmamış mesaj sayısı gibi sorgular REST ile yapılır.
 * → Client sayfa açıldığında önce REST ile geçmiş mesajları çeker,
 *   sonra WebSocket'e subscribe olarak yeni mesajları dinler.
 *
 * Yetkilendirme:
 * → anyRequest().authenticated() sayesinde tüm /api/v1/chat/** endpoint'leri JWT gerektirir.
 * → İş kuralı (proje sahipliği, başvuru kontrolü) service katmanında yapılır.
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatRestController {

    private final ChatMessageService chatMessageService;

    public ChatRestController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    /**
     * Proje bazlı mesaj geçmişi (paginated, yeniden eskiye).
     *
     * GET /api/v1/chat/projects/{projectId}/messages?page=0&size=20
     *
     * Client kullanım senaryosu:
     * 1. Proje sohbet ekranı açılır
     * 2. Bu endpoint'ten ilk sayfa (page=0, size=20) çekilir
     * 3. Infinite scroll ile page=1, page=2... çekilir
     * 4. Aynı anda WebSocket subscribe ile yeni mesajlar dinlenir
     */
    @GetMapping("/projects/{projectId}/messages")
    public ResponseEntity<RestResponse<Page<ChatMessageResponse>>> getProjectMessages(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        // Sayfa boyutunu sınırla (max 50)
        size = Math.min(size, 50);
        Pageable pageable = PageRequest.of(page, size);

        Page<ChatMessageResponse> messages = chatMessageService.getProjectMessages(
                projectId, authentication.getName(), pageable);

        return ResponseEntity.ok(RestResponse.success("Mesaj geçmişi başarıyla getirildi", messages));
    }

    /**
     * Proje bazlı okunmamış mesaj sayısı.
     *
     * GET /api/v1/chat/projects/{projectId}/unread-count
     *
     * Firma için: tedarikçi mesajlarının okunmamış sayısı
     * Tedarikçi için: firma mesajlarının okunmamış sayısı
     */
    @GetMapping("/projects/{projectId}/unread-count")
    public ResponseEntity<RestResponse<Long>> getUnreadCount(
            @PathVariable UUID projectId,
            Authentication authentication) {

        long count = chatMessageService.getUnreadCount(projectId, authentication.getName());

        return ResponseEntity.ok(RestResponse.success("Okunmamış mesaj sayısı", count));
    }
}
