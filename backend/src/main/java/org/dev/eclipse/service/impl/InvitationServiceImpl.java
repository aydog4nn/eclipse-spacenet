package org.dev.eclipse.service.impl;

import org.dev.eclipse.dto.mapper.InvitationMapper;
import org.dev.eclipse.dto.request.InvitationRequest;
import org.dev.eclipse.dto.request.InvitationStatusUpdateRequest;
import org.dev.eclipse.dto.response.InvitationResponse;
import org.dev.eclipse.entity.Invitation;
import org.dev.eclipse.entity.Organization;
import org.dev.eclipse.entity.Project;
import org.dev.eclipse.entity.User;
import org.dev.eclipse.enumarate.InvitationStatus;
import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.exception.CommonExceptions.BadRequestException;
import org.dev.eclipse.exception.UserExceptions.UserNotFoundException;
import org.dev.eclipse.repository.InvitationRepository;
import org.dev.eclipse.repository.OrganizationRepository;
import org.dev.eclipse.repository.ProjectRepository;
import org.dev.eclipse.repository.UserRepository;
import org.dev.eclipse.service.InvitationService;
import org.dev.eclipse.service.SecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;
    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final InvitationMapper invitationMapper;

    public InvitationServiceImpl(InvitationRepository invitationRepository,
                                 OrganizationRepository organizationRepository,
                                 ProjectRepository projectRepository,
                                 UserRepository userRepository,
                                 SecurityService securityService,
                                 InvitationMapper invitationMapper) {
        this.invitationRepository = invitationRepository;
        this.organizationRepository = organizationRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.securityService = securityService;
        this.invitationMapper = invitationMapper;
    }

    @Override
    @Transactional
    public InvitationResponse sendInvitation(UUID projectId, InvitationRequest request) {
        User user = getAuthenticatedUser();
        validateFirmRole(user);

        Project project = getProject(projectId);
        Organization firmOrganization = requireOrganization(user);
        if (!project.getOrganization().getId().equals(firmOrganization.getId())) {
            throw new BadRequestException("Sadece kendi ilaniniza davet gonderebilirsiniz.");
        }

        Organization supplierOrganization = organizationRepository.findById(request.supplierOrganizationId())
                .orElseThrow(() -> new BadRequestException("Tedarikci kurum bulunamadi."));

        if (supplierOrganization.getRole() != Role.TEDARIKCI) {
            throw new BadRequestException("Secilen kurum bir tedarikci degil.");
        }

        invitationRepository.findByProjectAndSupplierOrganization(project, supplierOrganization)
                .ifPresent(existing -> {
                    throw new BadRequestException("Bu kuruma zaten davet gonderildi.");
                });

        Invitation invitation = new Invitation();
        invitation.setProject(project);
        invitation.setSupplierOrganization(supplierOrganization);
        invitation.setMessage(trimToNull(request.message()));
        invitation.setStatus(InvitationStatus.BEKLIYOR);

        return invitationMapper.toResponse(invitationRepository.save(invitation));
    }

    @Override
    public List<InvitationResponse> getMyInvitations() {
        User user = getAuthenticatedUser();
        validateSupplierRole(user);

        return invitationRepository.findBySupplierOrganizationOrderByCreatedAtDesc(requireOrganization(user)).stream()
                .map(invitationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public InvitationResponse respondToInvitation(UUID invitationId, InvitationStatusUpdateRequest request) {
        User user = getAuthenticatedUser();
        validateSupplierRole(user);

        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new BadRequestException("Davet bulunamadi."));

        if (!invitation.getSupplierOrganization().getId().equals(requireOrganization(user).getId())) {
            throw new BadRequestException("Bu davet icin yetkiniz yok.");
        }

        if (request.status() == InvitationStatus.BEKLIYOR) {
            throw new BadRequestException("Davet durumu bekliyor olarak guncellenemez.");
        }

        invitation.setStatus(request.status());
        return invitationMapper.toResponse(invitationRepository.save(invitation));
    }

    private User getAuthenticatedUser() {
        UUID userId = securityService.getAuthenticatedUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanici bulunamadi"));
    }

    private Organization requireOrganization(User user) {
        if (user.getOrganization() == null) {
            throw new BadRequestException("Kullaniciya bagli kurum profili bulunamadi.");
        }
        return user.getOrganization();
    }

    private void validateSupplierRole(User user) {
        if (user.getRole() != Role.TEDARIKCI) {
            throw new BadRequestException("Bu islem sadece tedarikci hesaplari icindir.");
        }
    }

    private void validateFirmRole(User user) {
        if (user.getRole() != Role.FIRMA) {
            throw new BadRequestException("Bu islem sadece firma hesaplari icindir.");
        }
    }

    private Project getProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("Ilan bulunamadi."));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
