package org.dev.eclipse.repository;

import org.dev.eclipse.entity.ChatMessage;
import org.dev.eclipse.enumarate.SenderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * ChatMessage repository.
 *
 * Neden Page<ChatMessage> kullanıyoruz?
 * → Mesaj geçmişi potansiyel olarak binlerce kayıt içerebilir.
 * → Client infinite scroll veya pagination ile veri çeker.
 * → Page toplam kayıt sayısını da döner, frontend paginator için gerekli.
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /**
     * Proje bazlı mesaj geçmişi, yeniden eskiye sıralı, sayfalı.
     */
    Page<ChatMessage> findByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);

    /**
     * Okunmamış mesaj sayısı.
     * senderType filtresi: "karşı tarafın mesajları" sayılsın diye.
     * Örn: Firma kendi mesajlarını okunmamış saymamalı, sadece tedarikçi mesajlarını.
     */
    long countByProjectIdAndReadFalseAndSenderType(UUID projectId, SenderType senderType);
}
