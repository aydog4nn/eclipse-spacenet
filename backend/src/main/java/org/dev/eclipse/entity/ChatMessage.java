package org.dev.eclipse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import org.dev.eclipse.enumarate.SenderType;
import org.dev.eclipse.enumarate.StaticMessageCode;

/**
 * Proje bazlı sohbet mesajı entity'si.
 *
 * Neden ayrı bir entity?
 * → Mesajlar proje bazlı, hem firma hem tedarikçi gönderebilir.
 * → content alanı: firma için free-text, tedarikçi için server-generated.
 * → staticMessageCode: sadece tedarikçi mesajlarında dolu — hangi template seçildiğini audit trail olarak tutar.
 *
 * Index stratejisi:
 * → project_id + created_at DESC → mesaj geçmişi sorguları için composite index
 * → project_id tek başına → okunmamış mesaj sayısı sorguları için
 */
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_messages_project_id", columnList = "project_id"),
        @Index(name = "idx_chat_messages_project_created", columnList = "project_id, created_at DESC"),
        @Index(name = "idx_chat_messages_sender", columnList = "sender_user_id")
})
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_user_id", nullable = false)
    private User senderUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_organization_id", nullable = false)
    private Organization senderOrganization;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 20)
    private SenderType senderType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "static_message_code", length = 50)
    private StaticMessageCode staticMessageCode;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    public ChatMessage() {
    }

    // --- Getters & Setters ---

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getSenderUser() {
        return senderUser;
    }

    public void setSenderUser(User senderUser) {
        this.senderUser = senderUser;
    }

    public Organization getSenderOrganization() {
        return senderOrganization;
    }

    public void setSenderOrganization(Organization senderOrganization) {
        this.senderOrganization = senderOrganization;
    }

    public SenderType getSenderType() {
        return senderType;
    }

    public void setSenderType(SenderType senderType) {
        this.senderType = senderType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public StaticMessageCode getStaticMessageCode() {
        return staticMessageCode;
    }

    public void setStaticMessageCode(StaticMessageCode staticMessageCode) {
        this.staticMessageCode = staticMessageCode;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
