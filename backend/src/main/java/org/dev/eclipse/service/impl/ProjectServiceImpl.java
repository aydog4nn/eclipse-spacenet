package org.dev.eclipse.service.impl;

import org.dev.eclipse.dto.mapper.ProjectMapper;
import org.dev.eclipse.dto.request.ProjectRequest;
import org.dev.eclipse.dto.response.ProjectResponse;
import org.dev.eclipse.entity.Capability;
import org.dev.eclipse.entity.Organization;
import org.dev.eclipse.entity.Project;
import org.dev.eclipse.entity.User;
import org.dev.eclipse.enumarate.ProjectStatus;
import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.exception.CommonExceptions.BadRequestException;
import org.dev.eclipse.exception.UserExceptions.UserNotFoundException;
import org.dev.eclipse.repository.CapabilityRepository;
import org.dev.eclipse.repository.ProjectRepository;
import org.dev.eclipse.repository.UserRepository;
import org.dev.eclipse.service.ProjectService;
import org.dev.eclipse.service.SecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final CapabilityRepository capabilityRepository;
    private final SecurityService securityService;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              CapabilityRepository capabilityRepository,
                              SecurityService securityService,
                              UserRepository userRepository,
                              ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.capabilityRepository = capabilityRepository;
        this.securityService = securityService;
        this.userRepository = userRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        User user = getAuthenticatedUser();
        validateFirmRole(user);

        Project project = new Project();
        project.setOrganization(requireOrganization(user));
        applyRequest(project, request);

        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID projectId, ProjectRequest request) {
        User user = getAuthenticatedUser();
        validateFirmRole(user);

        Project project = getOwnedProject(projectId, requireOrganization(user));
        applyRequest(project, request);

        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    public void closeProject(UUID projectId) {
        User user = getAuthenticatedUser();
        validateFirmRole(user);

        Project project = getOwnedProject(projectId, requireOrganization(user));
        project.setStatus(ProjectStatus.KAPALI);
        projectRepository.save(project);
    }

    @Override
    public List<ProjectResponse> getMyProjects() {
        User user = getAuthenticatedUser();
        validateFirmRole(user);

        return projectRepository.findByOrganizationOrderByCreatedAtDesc(requireOrganization(user)).stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    @Override
    public List<ProjectResponse> getActiveProjects() {
        return projectRepository.findByStatusOrderByCreatedAtDesc(ProjectStatus.AKTIF).stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    @Override
    public ProjectResponse getProjectById(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("Ilan bulunamadi."));
        return projectMapper.toResponse(project);
    }

    private void applyRequest(Project project, ProjectRequest request) {
        project.setTitle(request.title().trim());
        project.setDescription(request.description().trim());
        project.setProjectType(request.projectType().trim());
        project.setCollaborationType(request.collaborationType());
        project.setRequiredCapabilities(resolveCapabilities(request.capabilityIds()));
        project.setPreferredSupplierCategory(request.preferredSupplierCategory());
        project.setLocationPreference(trimToNull(request.locationPreference()));
        project.setDurationText(trimToNull(request.durationText()));
        project.setBudgetRange(trimToNull(request.budgetRange()));
        project.setCertificationRequirements(normalizeSet(request.certificationRequirements()));
        project.setApplicationDeadline(request.applicationDeadline());
        project.setConfidentialityLevel(request.confidentialityLevel());
        project.setStatus(request.status());
    }

    private Set<Capability> resolveCapabilities(List<UUID> capabilityIds) {
        if (capabilityIds == null || capabilityIds.isEmpty()) {
            throw new BadRequestException("En az bir yetkinlik secilmelidir.");
        }

        List<Capability> capabilities = capabilityRepository.findAllById(capabilityIds);
        if (capabilities.size() != capabilityIds.size()) {
            throw new BadRequestException("Secilen yetkinliklerden biri bulunamadi.");
        }
        return new LinkedHashSet<>(capabilities);
    }

    private Set<String> normalizeSet(List<String> values) {
        if (values == null) {
            return new LinkedHashSet<>();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private User getAuthenticatedUser() {
        UUID userId = securityService.getAuthenticatedUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanici bulunamadi"));
    }

    private void validateFirmRole(User user) {
        if (user.getRole() != Role.FIRMA) {
            throw new BadRequestException("Bu islem sadece firma hesaplari icindir.");
        }
    }

    private Organization requireOrganization(User user) {
        if (user.getOrganization() == null) {
            throw new BadRequestException("Kullaniciya bagli kurum profili bulunamadi.");
        }
        return user.getOrganization();
    }

    private Project getOwnedProject(UUID projectId, Organization organization) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("Ilan bulunamadi."));

        if (!project.getOrganization().getId().equals(organization.getId())) {
            throw new BadRequestException("Bu ilana erisim yetkiniz yok.");
        }

        return project;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
