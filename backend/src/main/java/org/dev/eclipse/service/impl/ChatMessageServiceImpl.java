package org.dev.eclipse.service.impl;

import org.dev.eclipse.dto.request.CompanyMessageRequest;
import org.dev.eclipse.dto.request.SupplierMessageRequest;
import org.dev.eclipse.dto.response.ChatMessageResponse;
import org.dev.eclipse.entity.ChatMessage;
import org.dev.eclipse.entity.Organization;
import org.dev.eclipse.entity.Project;
import org.dev.eclipse.entity.User;
import org.dev.eclipse.enumarate.ApplicationStatus;
import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.enumarate.SenderType;
import org.dev.eclipse.exception.CommonExceptions.BadRequestException;
import org.dev.eclipse.repository.ApplicationRepository;
import org.dev.eclipse.repository.ChatMessageRepository;
import org.dev.eclipse.repository.ProjectRepository;
import org.dev.eclipse.repository.UserRepository;
import org.dev.eclipse.service.ChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Mesajlaşma iş mantığı implementasyonu.
 *
 * En kritik iş kuralı burada enforce edilir:
 * - Firma → free-text mesaj gönderebilir (content client'tan gelir)
 * - Tedarikçi → sadece staticMessageCode gönderir, content server üretir
 *
 * Erişim kontrolü:
 * - Firma: projenin sahibi olmalı
 * - Tedarikçi: projeye başvurusu olmalı (GERI_CEKILDI ve REDDEDILDI hariç)
 */
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageServiceImpl.class);

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;

    public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository,
                                  UserRepository userRepository,
                                  ProjectRepository projectRepository,
                                  ApplicationRepository applicationRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.applicationRepository = applicationRepository;
    }

    // ========================================================================
    // FIRMA MESAJI
    // ========================================================================

    @Override
    @Transactional
    public ChatMessageResponse sendCompanyMessage(String senderEmail, CompanyMessageRequest request) {
        User user = findUserByEmail(senderEmail);

        // Rol kontrolü: sadece FIRMA mesaj gönderebilir
        if (user.getRole() != Role.FIRMA) {
            throw new BadRequestException("Sadece firma hesapları free-text mesaj gönderebilir");
        }

        Project project = findProjectById(request.projectId());
        Organization org = user.getOrganization();

        if (org == null) {
            throw new BadRequestException("Kullanıcının bir organizasyonu bulunmuyor");
        }

        // Firma, projenin sahibi mi?
        if (!project.getOrganization().getId().equals(org.getId())) {
            throw new BadRequestException("Bu projenin sahibi değilsiniz, mesaj gönderemezsiniz");
        }

        // Persist
        ChatMessage message = new ChatMessage();
        message.setProject(project);
        message.setSenderUser(user);
        message.setSenderOrganization(org);
        message.setSenderType(SenderType.FIRMA);
        message.setContent(request.content().trim());
        message.setStaticMessageCode(null); // Firma free-text, static code yok

        ChatMessage saved = chatMessageRepository.save(message);
        logger.info("Firma mesajı kaydedildi: projectId={}, messageId={}", project.getId(), saved.getId());

        return toResponse(saved);
    }

    // ========================================================================
    // TEDARİKÇİ MESAJI
    // ========================================================================

    @Override
    @Transactional
    public ChatMessageResponse sendSupplierMessage(String senderEmail, SupplierMessageRequest request) {
        User user = findUserByEmail(senderEmail);

        // Rol kontrolü: sadece TEDARİKÇİ
        if (user.getRole() != Role.TEDARIKCI) {
            throw new BadRequestException("Sadece tedarikçi hesapları statik mesaj gönderebilir");
        }

        Project project = findProjectById(request.projectId());
        Organization org = user.getOrganization();

        if (org == null) {
            throw new BadRequestException("Kullanıcının bir organizasyonu bulunmuyor");
        }

        // Tedarikçinin bu projeye başvurusu var mı? (GERI_CEKILDI ve REDDEDILDI hariç)
        validateSupplierProjectAccess(project, org);

        // İçerik server tarafından üretilir — İŞ KURALININ MERKEZİ
        String content = request.staticMessageCode().getDisplayText();

        // Persist
        ChatMessage message = new ChatMessage();
        message.setProject(project);
        message.setSenderUser(user);
        message.setSenderOrganization(org);
        message.setSenderType(SenderType.TEDARIKCI);
        message.setContent(content); // Server-generated content
        message.setStaticMessageCode(request.staticMessageCode());

        ChatMessage saved = chatMessageRepository.save(message);
        logger.info("Tedarikçi mesajı kaydedildi: projectId={}, messageId={}, code={}",
                project.getId(), saved.getId(), request.staticMessageCode());

        return toResponse(saved);
    }

    // ========================================================================
    // MESAJ GEÇMİŞİ
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getProjectMessages(UUID projectId, String userEmail, Pageable pageable) {
        User user = findUserByEmail(userEmail);
        Project project = findProjectById(projectId);

        // Erişim kontrolü
        validateProjectAccess(user, project);

        return chatMessageRepository
                .findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(this::toResponse);
    }

    // ========================================================================
    // OKUNMAMIŞ MESAJ SAYISI
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID projectId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Project project = findProjectById(projectId);

        validateProjectAccess(user, project);

        // Firma ise tedarikçi mesajlarını say, tedarikçi ise firma mesajlarını say
        SenderType counterpartType = (user.getRole() == Role.FIRMA)
                ? SenderType.TEDARIKCI
                : SenderType.FIRMA;

        return chatMessageRepository.countByProjectIdAndReadFalseAndSenderType(projectId, counterpartType);
    }

    // ========================================================================
    // YARDIMCI METHODLAR
    // ========================================================================

    /**
     * Kullanıcının projeye erişim yetkisi var mı?
     * - FIRMA: Projenin sahibi olmalı
     * - TEDARİKÇİ: Projeye aktif başvurusu olmalı
     */
    @Override
    @Transactional(readOnly = true)
    public void authorizeProjectAccess(UUID projectId, String userEmail) {
        User user = findUserByEmail(userEmail);
        if (!user.isActive()) {
            throw new BadRequestException("Kullanıcı hesabı aktif değil");
        }
        validateProjectAccess(user, findProjectById(projectId));
    }

    private void validateProjectAccess(User user, Project project) {
        Organization org = user.getOrganization();
        if (org == null) {
            throw new BadRequestException("Kullanıcının bir organizasyonu bulunmuyor");
        }

        if (user.getRole() == Role.FIRMA) {
            if (!project.getOrganization().getId().equals(org.getId())) {
                throw new BadRequestException("Bu projeye erişim yetkiniz yok");
            }
        } else if (user.getRole() == Role.TEDARIKCI) {
            validateSupplierProjectAccess(project, org);
        } else {
            throw new BadRequestException("Bu rol için mesajlaşma desteklenmiyor");
        }
    }

    /**
     * Tedarikçinin projeye başvurusu olup olmadığını kontrol eder.
     * Sadece aktif başvurular (GERI_CEKILDI ve REDDEDILDI hariç) kabul edilir.
     */
    private void validateSupplierProjectAccess(Project project, Organization supplierOrg) {
        applicationRepository.findByProjectAndSupplierOrganization(project, supplierOrg)
                .ifPresentOrElse(
                        application -> {
                            if (application.getStatus() == ApplicationStatus.GERI_CEKILDI
                                    || application.getStatus() == ApplicationStatus.REDDEDILDI) {
                                throw new BadRequestException(
                                        "Bu proje için başvurunuz reddedilmiş veya geri çekilmiş durumda, mesaj gönderemezsiniz");
                            }
                        },
                        () -> {
                            throw new BadRequestException(
                                    "Bu projeye başvurunuz bulunmuyor, mesaj gönderemezsiniz");
                        }
                );
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Kullanıcı bulunamadı: " + email));
    }

    private Project findProjectById(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("Proje bulunamadı: " + projectId));
    }

    /**
     * Entity → Response DTO dönüşümü.
     * MapStruct kullanılabilir ama bu basit mapping için manual yeterli.
     */
    private ChatMessageResponse toResponse(ChatMessage msg) {
        return new ChatMessageResponse(
                msg.getId(),
                msg.getProject().getId(),
                msg.getSenderType(),
                msg.getSenderOrganization().getName(),
                msg.getContent(),
                msg.getStaticMessageCode(),
                msg.getCreatedAt()
        );
    }
}
