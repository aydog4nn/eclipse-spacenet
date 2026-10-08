package org.dev.eclipse.websocket;

import org.dev.eclipse.dto.request.CompanyMessageRequest;
import org.dev.eclipse.dto.request.SupplierMessageRequest;
import org.dev.eclipse.dto.response.ChatErrorResponse;
import org.dev.eclipse.dto.response.ChatMessageResponse;
import org.dev.eclipse.service.ChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * STOMP WebSocket Mesaj Controller'ı.
 *
 * Bu controller REST @RestController değil, STOMP @Controller.
 * HTTP endpoint'i yok. Sadece WebSocket üzerinden STOMP frame'leri işler.
 *
 * Mesaj akışı:
 * ┌─────────────────────────────────────────────────────────────┐
 * │ Client SEND /app/chat.sendCompany                          │
 * │   → ChatMessageController.handleCompanyMessage()           │
 * │     → ChatMessageService.sendCompanyMessage()              │
 * │       → validate + persist                                 │
 * │     → SimpMessagingTemplate.convertAndSend()               │
 * │       → /topic/projects/{projectId}/chat                   │
 * │         → herkes alır (firma + tedarikçi)                  │
 * └─────────────────────────────────────────────────────────────┘
 *
 * Hata durumunda:
 * ┌─────────────────────────────────────────────────────────────┐
 * │ @MessageExceptionHandler                                   │
 * │   → @SendToUser("/queue/errors")                           │
 * │     → SADECE hatayı atan client'a gönderilir               │
 * └─────────────────────────────────────────────────────────────┘
 *
 * Neden @MessageMapping ayrı firma/tedarikçi?
 * → İki farklı payload yapısı var (CompanyMessageRequest vs SupplierMessageRequest)
 * → İş kuralı tedarikçinin content alanı göndermesini engeller
 * → Tek endpoint olsa if/else ile rol kontrolü gerekir, ayrı endpoint daha temiz
 */
@Controller
public class ChatMessageController {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageController.class);

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageController(ChatMessageService chatMessageService,
                                 SimpMessagingTemplate messagingTemplate) {
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Firma (Company) free-text mesaj gönderir.
     *
     * Client gönderim:
     *   stompClient.publish({
     *     destination: '/app/chat.sendCompany',
     *     body: JSON.stringify({ projectId: '...', content: 'Merhaba' })
     *   });
     *
     * Başarılı olursa:
     *   → /topic/projects/{projectId}/chat kanalına broadcast edilir
     *
     * Principal:
     *   → WebSocketAuthInterceptor tarafından CONNECT'te set edilen kullanıcı email'i
     */
    @MessageMapping("chat.sendCompany")
    public void handleCompanyMessage(@Payload CompanyMessageRequest request,
                                     Principal principal) {
        logger.debug("Firma mesajı alındı: projectId={}, sender={}", request.projectId(), principal.getName());

        ChatMessageResponse response = chatMessageService.sendCompanyMessage(
                principal.getName(), request);

        // Broadcast: Projedeki herkese (firma + tedarikçiler)
        String destination = "/topic/projects/" + request.projectId() + "/chat";
        messagingTemplate.convertAndSend(destination, response);

        logger.info("Firma mesajı broadcast edildi: destination={}, messageId={}",
                destination, response.messageId());
    }

    /**
     * Tedarikçi (Supplier) statik mesaj kodu ile mesaj gönderir.
     *
     * Client gönderim:
     *   stompClient.publish({
     *     destination: '/app/chat.sendSupplier',
     *     body: JSON.stringify({ projectId: '...', staticMessageCode: 'FIYAT_BILGISI' })
     *   });
     *
     * Content alanı yok! Server StaticMessageCode enum'undan üretir.
     */
    @MessageMapping("chat.sendSupplier")
    public void handleSupplierMessage(@Payload SupplierMessageRequest request,
                                      Principal principal) {
        logger.debug("Tedarikçi mesajı alındı: projectId={}, sender={}, code={}",
                request.projectId(), principal.getName(), request.staticMessageCode());

        ChatMessageResponse response = chatMessageService.sendSupplierMessage(
                principal.getName(), request);

        // Broadcast: Projedeki herkese
        String destination = "/topic/projects/" + request.projectId() + "/chat";
        messagingTemplate.convertAndSend(destination, response);

        logger.info("Tedarikçi mesajı broadcast edildi: destination={}, messageId={}, code={}",
                destination, response.messageId(), response.staticMessageCode());
    }

    /**
     * STOMP mesaj işleme sırasında oluşan tüm hataları yakalar.
     * Hatayı SADECE gönderici client'a /user/queue/errors kanalı üzerinden iletir.
     *
     * @SendToUser → Spring, principal'dan user identity çıkarıp
     * /user/{sessionId}/queue/errors'a routelar. Sadece o session alır.
     *
     * Client subscribe:
     *   stompClient.subscribe('/user/queue/errors', (msg) => {
     *     const error = JSON.parse(msg.body);
     *     console.error(error.message);
     *   });
     */
    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public ChatErrorResponse handleException(Exception ex, Principal principal) {
        String userInfo = (principal != null) ? principal.getName() : "anonim";
        logger.error("WebSocket mesaj hatası [user={}]: {}", userInfo, ex.getMessage());

        return ChatErrorResponse.of("MESSAGING_ERROR", ex.getMessage());
    }
}
