package org.dev.eclipse.service.impl;

import org.dev.eclipse.dto.mapper.ApplicationMapper;
import org.dev.eclipse.dto.request.ApplicationRequest;
import org.dev.eclipse.dto.request.ApplicationStatusUpdateRequest;
import org.dev.eclipse.dto.response.ApplicationResponse;
import org.dev.eclipse.entity.Application;
import org.dev.eclipse.entity.Organization;
import org.dev.eclipse.entity.Project;
import org.dev.eclipse.entity.User;
import org.dev.eclipse.enumarate.ApplicationStatus;
import org.dev.eclipse.enumarate.ProjectStatus;
import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.exception.CommonExceptions.BadRequestException;
import org.dev.eclipse.exception.UserExceptions.UserNotFoundException;
import org.dev.eclipse.repository.ApplicationRepository;
import org.dev.eclipse.repository.ProjectRepository;
import org.dev.eclipse.repository.UserRepository;
import org.dev.eclipse.service.ApplicationService;
import org.dev.eclipse.service.SecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final ApplicationMapper applicationMapper;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  ProjectRepository projectRepository,
                                  UserRepository userRepository,
                                  SecurityService securityService,
                                  ApplicationMapper applicationMapper) {
        this.applicationRepository = applicationRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.securityService = securityService;
        this.applicationMapper = applicationMapper;
    }

    @Override
    @Transactional
    public ApplicationResponse applyToProject(UUID projectId, ApplicationRequest request) {
        User user = getAuthenticatedUser();
        validateSupplierRole(user);

        Organization supplierOrganization = requireOrganization(user);
        Project project = getProject(projectId);

        if (project.getStatus() != ProjectStatus.AKTIF) {
            throw new BadRequestException("Sadece aktif ilanlara basvuru yapilabilir.");
        }

        applicationRepository.findByProjectAndSupplierOrganization(project, supplierOrganization)
                .ifPresent(existing -> {
                    throw new BadRequestException("Bu ilana zaten basvuru yaptiniz.");
                });

        Application application = new Application();
        application.setProject(project);
        application.setSupplierOrganization(supplierOrganization);
        application.setShortDescription(request.shortDescription().trim());
        application.setWhyFit(request.whyFit().trim());
        application.setDocumentUrl(trimToNull(request.documentUrl()));
        application.setReferenceProjectNote(trimToNull(request.referenceProjectNote()));
        application.setStatus(ApplicationStatus.GONDERILDI);

        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    @Override
    @Transactional
    public void withdrawApplication(UUID applicationId) {
        User user = getAuthenticatedUser();
        validateSupplierRole(user);

        Application application = getApplication(applicationId);
        if (!application.getSupplierOrganization().getId().equals(requireOrganization(user).getId())) {
            throw new BadRequestException("Bu basvuru icin yetkiniz yok.");
        }

        application.setStatus(ApplicationStatus.GERI_CEKILDI);
        applicationRepository.save(application);
    }

    @Override
    public List<ApplicationResponse> getMyApplications() {
        User user = getAuthenticatedUser();
        validateSupplierRole(user);

        return applicationRepository.findBySupplierOrganizationOrderByCreatedAtDesc(requireOrganization(user)).stream()
                .map(applicationMapper::toResponse)
                .toList();
    }

    @Override
    public List<ApplicationResponse> getApplicationsForProject(UUID projectId) {
        User user = getAuthenticatedUser();
        validateFirmRole(user);

        Project project = getProject(projectId);
        if (!project.getOrganization().getId().equals(requireOrganization(user).getId())) {
            throw new BadRequestException("Bu ilana ait basvurulari goruntuleyemezsiniz.");
        }

        return applicationRepository.findByProjectOrderByCreatedAtDesc(project).stream()
                .map(applicationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ApplicationResponse updateApplicationStatus(UUID applicationId, ApplicationStatusUpdateRequest request) {
        User user = getAuthenticatedUser();
        validateFirmRole(user);

        Application application = getApplication(applicationId);
        if (!application.getProject().getOrganization().getId().equals(requireOrganization(user).getId())) {
            throw new BadRequestException("Bu basvuru icin yetkiniz yok.");
        }

        application.setStatus(request.status());
        return applicationMapper.toResponse(applicationRepository.save(application));
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

    private Application getApplication(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BadRequestException("Basvuru bulunamadi."));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
