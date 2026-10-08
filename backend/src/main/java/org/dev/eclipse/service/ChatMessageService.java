package org.dev.eclipse.service;

import org.dev.eclipse.dto.request.CompanyMessageRequest;
import org.dev.eclipse.dto.request.SupplierMessageRequest;
import org.dev.eclipse.dto.response.ChatMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Proje bazlı mesajlaşma servisi.
 *
 * Neden interface?
 * → Projenin mevcut yapısı interface → impl pattern kullanıyor.
 * → Test edilebilirlik, mock'lanabilirlik, gevşek bağlılık (loose coupling).
 */
public interface ChatMessageService {

    void authorizeProjectAccess(UUID projectId, String userEmail);

    /**
     * Firma (Company) free-text mesaj gönderir.
     *
     * @param senderEmail JWT'den çıkarılan gönderici email
     * @param request     projectId + content
     * @return broadcast edilecek mesaj response'u
     */
    ChatMessageResponse sendCompanyMessage(String senderEmail, CompanyMessageRequest request);

    /**
     * Tedarikçi (Supplier) statik mesaj kodu ile mesaj gönderir.
     * Content server tarafında üretilir.
     *
     * @param senderEmail JWT'den çıkarılan gönderici email
     * @param request     projectId + staticMessageCode
     * @return broadcast edilecek mesaj response'u
     */
    ChatMessageResponse sendSupplierMessage(String senderEmail, SupplierMessageRequest request);

    /**
     * Proje bazlı mesaj geçmişi (paginated, yeniden eskiye).
     *
     * @param projectId  proje ID
     * @param userEmail  erişim kontrolü için kullanıcı email
     * @param pageable   sayfa bilgisi
     * @return sayfalı mesaj listesi
     */
    Page<ChatMessageResponse> getProjectMessages(UUID projectId, String userEmail, Pageable pageable);

    /**
     * Proje bazlı okunmamış mesaj sayısı.
     * Firma için tedarikçi mesajları, tedarikçi için firma mesajları sayılır.
     *
     * @param projectId proje ID
     * @param userEmail kullanıcı email
     * @return okunmamış mesaj sayısı
     */
    long getUnreadCount(UUID projectId, String userEmail);
}
