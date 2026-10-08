package org.dev.eclipse.service.impl;

import org.dev.eclipse.annotation.Audit;
import org.dev.eclipse.dto.mapper.UserMapper;
import org.dev.eclipse.dto.request.ChangePasswordRequest;
import org.dev.eclipse.dto.request.FirmOnboardingBasicRequest;
import org.dev.eclipse.dto.request.FirmOnboardingDetailsRequest;
import org.dev.eclipse.dto.request.FirmProfileUpdateRequest;
import org.dev.eclipse.dto.request.SupplierOnboardingBasicRequest;
import org.dev.eclipse.dto.request.SupplierOnboardingCapabilitiesRequest;
import org.dev.eclipse.dto.request.SupplierOnboardingQualificationsRequest;
import org.dev.eclipse.dto.request.SupplierProfileUpdateRequest;
import org.dev.eclipse.dto.request.WebsiteSuggestionRequest;
import org.dev.eclipse.dto.response.OnboardingStatusResponse;
import org.dev.eclipse.dto.response.FileResourceResponse;
import org.dev.eclipse.dto.response.OrganizationProfileResponse;
import org.dev.eclipse.dto.response.WebsiteSuggestionResponse;
import org.dev.eclipse.dto.response.UploadedFileResponse;
import org.dev.eclipse.entity.Organization;
import org.dev.eclipse.entity.User;
import org.dev.eclipse.enumarate.OnboardingStep;
import org.dev.eclipse.exception.AuthExceptions.AuthException;
import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.exception.CommonExceptions.BadRequestException;
import org.dev.eclipse.exception.UserExceptions.UserNotFoundException;
import org.dev.eclipse.repository.UserRepository;
import org.dev.eclipse.service.OrganizationSearchService;
import org.dev.eclipse.service.FileService;
import org.dev.eclipse.service.SecurityService;
import org.dev.eclipse.service.UserService;
import org.dev.eclipse.service.WebsiteSuggestionService;
import org.dev.eclipse.service.async.AsyncMailDispatcher;
import org.dev.eclipse.util.FileUploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;


@Service
@Validated
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final PasswordEncoder passwordEncoder;
    private final AsyncMailDispatcher asyncMailDispatcher;
    private final OrganizationSearchService organizationSearchService;
    private final UserMapper userMapper;
    private final FileService fileService;
    private final WebsiteSuggestionService websiteSuggestionService;


    public UserServiceImpl(UserRepository userRepository,
                           SecurityService securityService,
                           PasswordEncoder passwordEncoder,
                           AsyncMailDispatcher asyncMailDispatcher,
                           OrganizationSearchService organizationSearchService,
                           UserMapper userMapper,
                           WebsiteSuggestionService websiteSuggestionService,
                           FileService fileService) {
        this.userRepository = userRepository;
        this.securityService = securityService;
        this.passwordEncoder = passwordEncoder;
        this.asyncMailDispatcher = asyncMailDispatcher;
        this.organizationSearchService = organizationSearchService;
        this.userMapper = userMapper;
        this.websiteSuggestionService = websiteSuggestionService;
        this.fileService = fileService;
    }

    @Audit(action = "GET_CURRENT_PROFILE", resource = "Organization")
    public OrganizationProfileResponse getCurrentProfile() {
        UUID userId = securityService.getAuthenticatedUserId();
        logger.info("Fetching current organization profile for userId={}", userId);

        User user = getAuthenticatedUserEntity(userId);
        return userMapper.toOrganizationProfileResponse(user);
    }

    @Override
    public List<OrganizationProfileResponse> getPublicOrganizations() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() != Role.ADMIN)
                .filter(User::isActive)
                .filter(user -> user.getOrganization() != null)
                .filter(user -> user.getOrganization().isVisible())
                .map(userMapper::toOrganizationProfileResponse)
                .sorted(Comparator.comparing(OrganizationProfileResponse::organizationName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public OrganizationProfileResponse getPublicOrganizationProfile(UUID organizationId) {
        User user = userRepository.findByOrganization_Id(organizationId)
                .filter(User::isActive)
                .filter(value -> value.getRole() != Role.ADMIN)
                .filter(value -> value.getOrganization() != null && value.getOrganization().isVisible())
                .orElseThrow(() -> new UserNotFoundException("Kurum profili bulunamadi"));

        return userMapper.toOrganizationProfileResponse(user);
    }

    @Override
    @Audit(action = "GET_ONBOARDING_STATUS", resource = "Organization")
    public OnboardingStatusResponse getOnboardingStatus() {
        User user = getAuthenticatedUserEntity(securityService.getAuthenticatedUserId());
        Organization organization = requireOrganization(user);
        return new OnboardingStatusResponse(
                user.getRole(),
                organization.isApproved(),
                organization.isOnboardingCompleted(),
                organization.getOnboardingStep(),
                userMapper.toOrganizationProfileResponse(user).completionPercentage()
        );
    }

    @Override
    @Transactional
    @Audit(action = "COMPLETE_FIRM_BASIC_ONBOARDING", resource = "Organization")
    public void completeFirmBasicOnboarding(FirmOnboardingBasicRequest request) {
        User user = getAuthenticatedUserEntity(securityService.getAuthenticatedUserId());
        validateRole(user, Role.FIRMA);
        Organization organization = requireOrganization(user);
        organization.setName(trimToNull(request.organizationName()));
        organization.setDescription(trimToNull(request.description()));
        organization.setActivityArea(trimToNull(request.activityArea()));
        organization.setLocation(trimToNull(request.location()));
        organization.setWebsite(trimToNull(request.website()));
        organization.setPhoneNumber(trimToNull(request.phoneNumber()));
        organization.setContactEmail(trimToNull(request.contactEmail()));
        organization.setOnboardingStep(OnboardingStep.CAPABILITIES);
        persistAndIndex(user, organization);
    }

    @Override
    @Transactional
    @Audit(action = "COMPLETE_FIRM_DETAILS_ONBOARDING", resource = "Organization")
    public void completeFirmDetailsOnboarding(FirmOnboardingDetailsRequest request) {
        User user = getAuthenticatedUserEntity(securityService.getAuthenticatedUserId());
        validateRole(user, Role.FIRMA);
        Organization organization = requireOrganization(user);
        organization.setFocusAreas(normalizeList(request.focusAreas()));
        organization.setCertifications(normalizeList(request.certifications()));
        organization.setReferenceProjects(normalizeList(request.referenceProjects()));
        organization.setOnboardingStep(OnboardingStep.COMPLETED);
        organization.setOnboardingCompleted(true);
        persistAndIndex(user, organization);
    }

    @Override
    @Transactional
    @Audit(action = "COMPLETE_SUPPLIER_BASIC_ONBOARDING", resource = "Organization")
    public void completeSupplierBasicOnboarding(SupplierOnboardingBasicRequest request) {
        User user = getAuthenticatedUserEntity(securityService.getAuthenticatedUserId());
        validateRole(user, Role.TEDARIKCI);
        Organization organization = requireOrganization(user);
        organization.setName(trimToNull(request.organizationName()));
        organization.setSupplierCategory(request.supplierCategory());
        organization.setDescription(trimToNull(request.description()));
        organization.setLocation(trimToNull(request.location()));
        organization.setWebsite(trimToNull(request.website()));
        organization.setPhoneNumber(trimToNull(request.phoneNumber()));
        organization.setContactEmail(trimToNull(request.contactEmail()));
        organization.setTeamSize(request.teamSize());
        organization.setOnboardingStep(OnboardingStep.CAPABILITIES);
        persistAndIndex(user, organization);
    }

    @Override
    @Transactional
    @Audit(action = "COMPLETE_SUPPLIER_CAPABILITIES_ONBOARDING", resource = "Organization")
    public void completeSupplierCapabilitiesOnboarding(SupplierOnboardingCapabilitiesRequest request) {
        User user = getAuthenticatedUserEntity(securityService.getAuthenticatedUserId());
        validateRole(user, Role.TEDARIKCI);
        Organization organization = requireOrganization(user);
        organization.setFocusAreas(normalizeList(request.focusAreas()));
        organization.setExpertiseAreas(normalizeList(request.expertiseAreas()));
        organization.setHardwareCapabilities(normalizeList(request.hardwareCapabilities()));
        organization.setSoftwareCapabilities(normalizeList(request.softwareCapabilities()));
        organization.setRdCapabilities(normalizeList(request.rdCapabilities()));
        organization.setProductionInfrastructure(normalizeList(request.productionInfrastructure()));
        organization.setTestInfrastructure(normalizeList(request.testInfrastructure()));
        organization.setOnboardingStep(OnboardingStep.QUALIFICATIONS);
        persistAndIndex(user, organization);
    }

    @Override
    @Transactional
    @Audit(action = "COMPLETE_SUPPLIER_QUALIFICATIONS_ONBOARDING", resource = "Organization")
    public void completeSupplierQualificationsOnboarding(SupplierOnboardingQualificationsRequest request) {
        User user = getAuthenticatedUserEntity(securityService.getAuthenticatedUserId());
        validateRole(user, Role.TEDARIKCI);
        Organization organization = requireOrganization(user);
        organization.setCertifications(normalizeList(request.certifications()));
        organization.setReferenceProjects(normalizeList(request.referenceProjects()));
        organization.setServiceAreas(normalizeList(request.serviceAreas()));
        organization.setOnboardingStep(OnboardingStep.COMPLETED);
        organization.setOnboardingCompleted(true);
        persistAndIndex(user, organization);
    }

    @Override
    @Audit(action = "GENERATE_WEBSITE_SUGGESTIONS", resource = "Organization")
    public WebsiteSuggestionResponse generateWebsiteSuggestions(WebsiteSuggestionRequest request) {
        User user = getAuthenticatedUserEntity(securityService.getAuthenticatedUserId());
        return websiteSuggestionService.analyze(request.website(), requireOrganization(user));
    }

    @Override
    @Transactional
    @Audit(action = "UPDATE_FIRM_PROFILE", resource = "Organization")
    public void updateFirmProfile(FirmProfileUpdateRequest request) {
        UUID userId = securityService.getAuthenticatedUserId();
        logger.info("Updating firm profile for userId={}", userId);

        User user = getAuthenticatedUserEntity(userId);
        validateRole(user, Role.FIRMA);
        Organization organization = requireOrganization(user);
        userMapper.updateFirmOrganization(organization, request);
        persistAndIndex(user, organization);

        logger.info("Firm profile updated successfully for userId={}", userId);
    }

    @Override
    @Transactional
    @Audit(action = "UPDATE_SUPPLIER_PROFILE", resource = "Organization")
    public void updateSupplierProfile(SupplierProfileUpdateRequest request) {
        UUID userId = securityService.getAuthenticatedUserId();
        logger.info("Updating supplier profile for userId={}", userId);

        User user = getAuthenticatedUserEntity(userId);
        validateRole(user, Role.TEDARIKCI);
        Organization organization = requireOrganization(user);
        userMapper.updateSupplierOrganization(organization, request);
        persistAndIndex(user, organization);

        logger.info("Supplier profile updated successfully for userId={}", userId);
    }

    @Override
    @Transactional
    @Audit(action = "UPLOAD_SUPPLIER_CERTIFICATE", resource = "Organization")
    public UploadedFileResponse uploadSupplierCertificate(MultipartFile file, String displayName) {
        UUID userId = securityService.getAuthenticatedUserId();
        logger.info("Uploading supplier certificate for userId={}", userId);

        User user = getAuthenticatedUserEntity(userId);
        validateRole(user, Role.TEDARIKCI);
        Organization organization = requireOrganization(user);

        try {
            String folderName = "supplier-certificates/" + organization.getId();
            String filePath = FileUploadUtil.saveSingleFile(file, folderName);
            if (filePath == null || filePath.isBlank()) {
                throw new BadRequestException("Yuklenecek sertifika dosyasi bulunamadi.");
            }

            if (organization.getCertificateFileUrls().stream().noneMatch(filePath::equals)) {
                organization.getCertificateFileUrls().add(filePath);
                organization.getCertificateFileLabels().add(
                        (displayName == null || displayName.isBlank()) ? file.getOriginalFilename() : displayName.trim()
                );
            }

            if (displayName != null && !displayName.isBlank()
                    && organization.getCertifications().stream().noneMatch(existing -> existing.equalsIgnoreCase(displayName.trim()))) {
                organization.getCertifications().add(displayName.trim());
            }

            userRepository.save(user);
            organizationSearchService.indexOrganization(organization);

            return new UploadedFileResponse(
                    filePath,
                    "/api/v1/files/" + filePath.replaceFirst("^uploads/", ""),
                    (displayName == null || displayName.isBlank()) ? file.getOriginalFilename() : displayName.trim()
            );
        } catch (IOException ex) {
            logger.error("Supplier certificate upload failed for userId={}", userId, ex);
            throw new BadRequestException("Sertifika dosyasi yuklenemedi.");
        }
    }

    @Override
    @Transactional
    @Audit(action = "DELETE_SUPPLIER_CERTIFICATE", resource = "Organization")
    public void deleteSupplierCertificate(String fileUrl) {
        UUID userId = securityService.getAuthenticatedUserId();
        logger.info("Deleting supplier certificate for userId={}", userId);

        User user = getAuthenticatedUserEntity(userId);
        validateRole(user, Role.TEDARIKCI);
        Organization organization = requireOrganization(user);

        String normalizedPath = normalizeStoredCertificatePath(fileUrl);
        if (normalizedPath.isBlank()) {
            throw new BadRequestException("Silinecek sertifika dosyasi bulunamadi.");
        }

        int removedIndex = -1;
        for (int index = 0; index < organization.getCertificateFileUrls().size(); index++) {
            String existing = organization.getCertificateFileUrls().get(index);
            if (existing != null && existing.equalsIgnoreCase(normalizedPath)) {
                removedIndex = index;
                break;
            }
        }

        if (removedIndex < 0) {
            throw new BadRequestException("Sertifika dosyasi profilde bulunamadi.");
        }

        organization.getCertificateFileUrls().remove(removedIndex);
        if (organization.getCertificateFileLabels().size() > removedIndex) {
            organization.getCertificateFileLabels().remove(removedIndex);
        }

        userRepository.save(user);
        organizationSearchService.indexOrganization(organization);

        try {
            Files.deleteIfExists(Paths.get(normalizedPath));
        } catch (IOException ex) {
            logger.warn("Uploaded supplier certificate file could not be deleted from disk: {}", normalizedPath, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Audit(action = "VIEW_SUPPLIER_CERTIFICATE", resource = "Organization")
    public FileResourceResponse getSupplierCertificateResource(String fileUrl) {
        UUID userId = securityService.getAuthenticatedUserId();
        logger.info("Viewing supplier certificate for userId={}", userId);

        User user = getAuthenticatedUserEntity(userId);
        validateRole(user, Role.TEDARIKCI);
        Organization organization = requireOrganization(user);

        String normalizedPath = normalizeStoredCertificatePath(fileUrl);
        if (normalizedPath.isBlank()) {
            throw new BadRequestException("Goruntulenecek sertifika dosyasi bulunamadi.");
        }

        boolean exists = organization.getCertificateFileUrls().stream().anyMatch(
                existing -> existing != null && existing.equalsIgnoreCase(normalizedPath)
        );
        if (!exists) {
            throw new BadRequestException("Bu sertifika dosyasina erisim izniniz yok.");
        }

        return fileService.getFileResource(normalizedPath.replaceFirst("^uploads/", ""));
    }

    @Override
    @Transactional
    @Audit(action = "CHANGE_PASSWORD", resource = "User")
    public void changePassword(ChangePasswordRequest request) {
        UUID userId = securityService.getAuthenticatedUserId();
        logger.info("Password change requested for userId={}", userId);

        User user = getAuthenticatedUserEntity(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            logger.warn("Password change failed due to incorrect current password for userId={}", userId);
            throw new AuthException("Mevcut sifreniz hatali");
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            logger.warn("Password change failed due to password mismatch for userId={}", userId);
            throw new AuthException("Yeni sifreler birbiriyle uyusmuyor");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        asyncMailDispatcher.sendPasswordChangedNotification(user.getEmail());
        logger.info("Password changed successfully and notification sent for userId={}", userId);
    }

    @Override
    @Transactional
    @Audit(action = "DEACTIVATE_USER", resource = "User")
    public void deactivateAuthenticatedUser() {
        UUID userId = securityService.getAuthenticatedUserId();
        logger.info("Deactivating user with id={}", userId);

        User user = getAuthenticatedUserEntity(userId);

        user.setActive(false);
        userRepository.save(user);
        if (user.getOrganization() != null) {
            organizationSearchService.removeOrganization(user.getOrganization().getId());
        }

        logger.info("User with id {} has been deactivated.", userId);
    }

    private User getAuthenticatedUserEntity(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found for id={}", userId);
                    return new UserNotFoundException("Kullanici bulunamadi");
                });
    }

    private void validateRole(User user, Role expectedRole) {
        if (user.getRole() != expectedRole) {
            throw new BadRequestException("Bu profil islemi mevcut rol icin uygun degil.");
        }
    }

    private void persistAndIndex(User user, Organization organization) {
        userRepository.save(user);
        organizationSearchService.indexOrganization(organization);
    }

    private Organization requireOrganization(User user) {
        if (user.getOrganization() == null) {
            throw new BadRequestException("Kullaniciya ait kurum profili bulunamadi.");
        }
        return user.getOrganization();
    }

    private String normalizeStoredCertificatePath(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return "";
        }

        String normalized = fileUrl.trim()
                .replaceFirst("^https?://[^/]+", "")
                .replaceFirst("^/api/v1/files/?", "")
                .replaceFirst("^/uploads/?", "")
                .replaceFirst("^uploads/?", "");

        if (normalized.isBlank()) {
            return "";
        }

        Path relativePath = Paths.get(normalized).normalize();
        return Paths.get("uploads").resolve(relativePath).normalize().toString();
    }

    private List<String> normalizeList(List<String> values) {
        if (values == null) {
            return java.util.List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
