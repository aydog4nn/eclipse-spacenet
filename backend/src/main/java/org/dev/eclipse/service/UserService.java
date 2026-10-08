package org.dev.eclipse.service;

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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserService {
    OrganizationProfileResponse getCurrentProfile();

    OnboardingStatusResponse getOnboardingStatus();

    void completeFirmBasicOnboarding(FirmOnboardingBasicRequest request);

    void completeFirmDetailsOnboarding(FirmOnboardingDetailsRequest request);

    void completeSupplierBasicOnboarding(SupplierOnboardingBasicRequest request);

    void completeSupplierCapabilitiesOnboarding(SupplierOnboardingCapabilitiesRequest request);

    void completeSupplierQualificationsOnboarding(SupplierOnboardingQualificationsRequest request);

    WebsiteSuggestionResponse generateWebsiteSuggestions(WebsiteSuggestionRequest request);

    List<OrganizationProfileResponse> getPublicOrganizations();

    OrganizationProfileResponse getPublicOrganizationProfile(UUID organizationId);

    void updateFirmProfile(FirmProfileUpdateRequest request);

    void updateSupplierProfile(SupplierProfileUpdateRequest request);

    UploadedFileResponse uploadSupplierCertificate(MultipartFile file, String displayName);

    void deleteSupplierCertificate(String fileUrl);

    FileResourceResponse getSupplierCertificateResource(String fileUrl);

    void changePassword(ChangePasswordRequest request);

    void deactivateAuthenticatedUser();
}
