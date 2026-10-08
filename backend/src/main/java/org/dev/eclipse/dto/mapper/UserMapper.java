package org.dev.eclipse.dto.mapper;

import org.dev.eclipse.dto.request.FirmProfileUpdateRequest;
import org.dev.eclipse.dto.request.FirmRegisterRequest;
import org.dev.eclipse.dto.request.SupplierProfileUpdateRequest;
import org.dev.eclipse.dto.request.SupplierRegisterRequest;
import org.dev.eclipse.dto.response.OrganizationProfileResponse;
import org.dev.eclipse.dto.response.UserResponse;
import org.dev.eclipse.entity.Organization;
import org.dev.eclipse.entity.User;
import org.dev.eclipse.enumarate.OnboardingStep;
import org.dev.eclipse.enumarate.Role;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Objects;

@Mapper(config = MapStructConfig.class)
public abstract class UserMapper {

    @Mapping(target = "role", constant = "FIRMA")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "organization", expression = "java(toFirmOrganization(request))")
    public abstract User toFirmEntity(FirmRegisterRequest request);

    @Mapping(target = "role", constant = "TEDARIKCI")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "organization", expression = "java(toSupplierOrganization(request))")
    public abstract User toSupplierEntity(SupplierRegisterRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationName", source = "organization.name")
    @Mapping(target = "phoneNumber", source = "organization.phoneNumber")
    @Mapping(target = "location", source = "organization.location")
    @Mapping(target = "website", source = "organization.website")
    @Mapping(target = "supplierCategory", source = "organization.supplierCategory")
    @Mapping(target = "focusAreas", expression = "java(safeList(user.getOrganization() != null ? user.getOrganization().getFocusAreas() : null))")
    @Mapping(target = "verified", expression = "java(user.getOrganization() != null && user.getOrganization().isVerified())")
    @Mapping(target = "approved", expression = "java(user.getOrganization() != null && user.getOrganization().isApproved())")
    @Mapping(target = "onboardingCompleted", expression = "java(user.getOrganization() != null && user.getOrganization().isOnboardingCompleted())")
    @Mapping(target = "onboardingStep", expression = "java(user.getOrganization() != null ? user.getOrganization().getOnboardingStep() : null)")
    public abstract UserResponse toResponse(User user);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationName", source = "organization.name")
    @Mapping(target = "supplierCategory", source = "organization.supplierCategory")
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "description", source = "organization.description")
    @Mapping(target = "logoUrl", source = "organization.logoUrl")
    @Mapping(target = "activityArea", source = "organization.activityArea")
    @Mapping(target = "location", source = "organization.location")
    @Mapping(target = "website", source = "organization.website")
    @Mapping(target = "phoneNumber", source = "organization.phoneNumber")
    @Mapping(target = "contactEmail", source = "organization.contactEmail")
    @Mapping(target = "teamSize", source = "organization.teamSize")
    @Mapping(target = "focusAreas", expression = "java(safeList(user.getOrganization() != null ? user.getOrganization().getFocusAreas() : null))")
    @Mapping(target = "certifications", expression = "java(safeList(user.getOrganization() != null ? user.getOrganization().getCertifications() : null))")
    @Mapping(target = "referenceProjects", expression = "java(safeList(user.getOrganization() != null ? user.getOrganization().getReferenceProjects() : null))")
    @Mapping(target = "certificateFileUrls", expression = "java(safeList(user.getOrganization() != null ? user.getOrganization().getCertificateFileUrls() : null))")
    @Mapping(target = "certificateFileLabels", expression = "java(safeList(user.getOrganization() != null ? user.getOrganization().getCertificateFileLabels() : null))")
    @Mapping(target = "expertiseAreas", expression = "java(safeList(user.getOrganization() != null ? user.getOrganization().getExpertiseAreas() : null))")
    @Mapping(target = "hardwareCapabilities", expression = "java(safeList(user.getOrganization() != null ? user.getOrganization().getHardwareCapabilities() : null))")
    @Mapping(target = "softwareCapabilities", expression = "java(safeList(user.getOrganization() != null ? user.getOrganization().getSoftwareCapabilities() : null))")
    @Mapping(target = "rdCapabilities", expression = "java(safeList(user.getOrganization() != null ? user.getOrganization().getRdCapabilities() : null))")
    @Mapping(target = "productionInfrastructure", expression = "java(safeList(user.getOrganization() != null ? user.getOrganization().getProductionInfrastructure() : null))")
    @Mapping(target = "testInfrastructure", expression = "java(safeList(user.getOrganization() != null ? user.getOrganization().getTestInfrastructure() : null))")
    @Mapping(target = "serviceAreas", expression = "java(safeList(user.getOrganization() != null ? user.getOrganization().getServiceAreas() : null))")
    @Mapping(target = "verified", expression = "java(user.getOrganization() != null && user.getOrganization().isVerified())")
    @Mapping(target = "approved", expression = "java(user.getOrganization() != null && user.getOrganization().isApproved())")
    @Mapping(target = "onboardingCompleted", expression = "java(user.getOrganization() != null && user.getOrganization().isOnboardingCompleted())")
    @Mapping(target = "onboardingStep", expression = "java(user.getOrganization() != null ? user.getOrganization().getOnboardingStep() : null)")
    @Mapping(target = "visible", expression = "java(user.getOrganization() == null || user.getOrganization().isVisible())")
    @Mapping(target = "completionPercentage", expression = "java(calculateCompletionPercentage(user))")
    public abstract OrganizationProfileResponse toOrganizationProfileResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "name", expression = "java(trimToNull(request.organizationName()))")
    @Mapping(target = "description", expression = "java(trimToNull(request.description()))")
    @Mapping(target = "logoUrl", expression = "java(trimToNull(request.logoUrl()))")
    @Mapping(target = "activityArea", expression = "java(trimToNull(request.activityArea()))")
    @Mapping(target = "location", expression = "java(trimToNull(request.location()))")
    @Mapping(target = "website", expression = "java(trimToNull(request.website()))")
    @Mapping(target = "phoneNumber", expression = "java(trimToNull(request.phoneNumber()))")
    @Mapping(target = "contactEmail", expression = "java(trimToNull(request.contactEmail()))")
    @Mapping(target = "certifications", expression = "java(normalizeList(request.certifications()))")
    @Mapping(target = "referenceProjects", expression = "java(normalizeList(request.referenceProjects()))")
    @Mapping(target = "focusAreas", expression = "java(normalizeList(request.focusAreas()))")
    @Mapping(target = "visible", source = "visible")
    public abstract void updateFirmOrganization(@MappingTarget Organization organization, FirmProfileUpdateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "name", expression = "java(trimToNull(request.organizationName()))")
    @Mapping(target = "supplierCategory", source = "supplierCategory")
    @Mapping(target = "description", expression = "java(trimToNull(request.description()))")
    @Mapping(target = "logoUrl", expression = "java(trimToNull(request.logoUrl()))")
    @Mapping(target = "location", expression = "java(trimToNull(request.location()))")
    @Mapping(target = "website", expression = "java(trimToNull(request.website()))")
    @Mapping(target = "phoneNumber", expression = "java(trimToNull(request.phoneNumber()))")
    @Mapping(target = "contactEmail", expression = "java(trimToNull(request.contactEmail()))")
    @Mapping(target = "teamSize", source = "teamSize")
    @Mapping(target = "expertiseAreas", expression = "java(normalizeList(request.expertiseAreas()))")
    @Mapping(target = "hardwareCapabilities", expression = "java(normalizeList(request.hardwareCapabilities()))")
    @Mapping(target = "softwareCapabilities", expression = "java(normalizeList(request.softwareCapabilities()))")
    @Mapping(target = "rdCapabilities", expression = "java(normalizeList(request.rdCapabilities()))")
    @Mapping(target = "productionInfrastructure", expression = "java(normalizeList(request.productionInfrastructure()))")
    @Mapping(target = "testInfrastructure", expression = "java(normalizeList(request.testInfrastructure()))")
    @Mapping(target = "certifications", expression = "java(normalizeList(request.certifications()))")
    @Mapping(target = "referenceProjects", expression = "java(normalizeList(request.referenceProjects()))")
    @Mapping(target = "serviceAreas", expression = "java(normalizeList(request.serviceAreas()))")
    @Mapping(target = "focusAreas", expression = "java(normalizeList(request.focusAreas()))")
    @Mapping(target = "visible", source = "visible")
    public abstract void updateSupplierOrganization(@MappingTarget Organization organization, SupplierProfileUpdateRequest request);

    protected Organization toFirmOrganization(FirmRegisterRequest request) {
        Organization organization = new Organization();
        organization.setName(trimToNull(request.organizationName()));
        organization.setRole(Role.FIRMA);
        organization.setPhoneNumber(trimToNull(request.phoneNumber()));
        organization.setLocation(trimToNull(request.location()));
        organization.setWebsite(trimToNull(request.website()));
        organization.setVisible(true);
        organization.setVerified(false);
        organization.setApproved(false);
        organization.setOnboardingStep(OnboardingStep.BASIC_INFO);
        organization.setOnboardingCompleted(false);
        return organization;
    }

    protected Organization toSupplierOrganization(SupplierRegisterRequest request) {
        Organization organization = new Organization();
        organization.setName(trimToNull(request.organizationName()));
        organization.setRole(Role.TEDARIKCI);
        organization.setSupplierCategory(request.supplierCategory());
        organization.setPhoneNumber(trimToNull(request.phoneNumber()));
        organization.setLocation(trimToNull(request.location()));
        organization.setWebsite(trimToNull(request.website()));
        organization.setVisible(true);
        organization.setVerified(false);
        organization.setApproved(false);
        organization.setOnboardingStep(OnboardingStep.BASIC_INFO);
        organization.setOnboardingCompleted(false);
        organization.setFocusAreas(normalizeList(request.focusAreas()));
        return organization;
    }

    protected int calculateCompletionPercentage(User user) {
        Organization organization = user.getOrganization();
        if (organization == null) {
            return 0;
        }

        int totalFields = user.getRole() == Role.FIRMA ? 9 : 13;
        int completed = 0;

        if (hasText(organization.getName())) completed++;
        if (hasText(organization.getDescription())) completed++;
        if (hasText(organization.getLocation())) completed++;
        if (hasText(organization.getWebsite())) completed++;
        if (hasText(organization.getPhoneNumber())) completed++;
        if (!organization.getCertifications().isEmpty()) completed++;
        if (!organization.getReferenceProjects().isEmpty()) completed++;
        if (!organization.getFocusAreas().isEmpty()) completed++;
        if (hasText(organization.getLogoUrl())) completed++;

        if (user.getRole() == Role.TEDARIKCI) {
            if (organization.getSupplierCategory() != null) completed++;
            if (!organization.getExpertiseAreas().isEmpty()) completed++;
            if (!organization.getHardwareCapabilities().isEmpty()
                    || !organization.getSoftwareCapabilities().isEmpty()
                    || !organization.getRdCapabilities().isEmpty()) completed++;
            if (!organization.getProductionInfrastructure().isEmpty()
                    || !organization.getTestInfrastructure().isEmpty()) completed++;
        }

        return (int) Math.round((completed * 100.0) / totalFields);
    }

    protected List<String> normalizeList(List<String> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    protected List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }

    protected String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    protected boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
