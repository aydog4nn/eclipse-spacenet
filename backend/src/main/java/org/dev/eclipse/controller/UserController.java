package org.dev.eclipse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.dev.eclipse.dto.response.RestResponse;
import org.dev.eclipse.dto.response.WebsiteSuggestionResponse;
import org.dev.eclipse.dto.response.UploadedFileResponse;
import org.springframework.core.io.Resource;
import org.dev.eclipse.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "Profile Management", description = "Organization profile and account management endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get user profile", description = "Retrieve the current authenticated user's profile information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile information retrieved successfully", content = @Content(schema = @Schema(implementation = OrganizationProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @GetMapping("/profile")
    public ResponseEntity<RestResponse<OrganizationProfileResponse>> getProfile() {
        OrganizationProfileResponse profile = userService.getCurrentProfile();
        return ResponseEntity.ok(RestResponse.success("Profile information retrieved", profile));
    }

    @GetMapping("/onboarding")
    public ResponseEntity<RestResponse<OnboardingStatusResponse>> getOnboardingStatus() {
        return ResponseEntity.ok(RestResponse.success("Onboarding status retrieved", userService.getOnboardingStatus()));
    }

    @PutMapping("/onboarding/firm/basic")
    public ResponseEntity<RestResponse<String>> completeFirmBasicOnboarding(@Valid @RequestBody FirmOnboardingBasicRequest request) {
        userService.completeFirmBasicOnboarding(request);
        return ResponseEntity.ok(RestResponse.success("Firm basic onboarding completed"));
    }

    @PutMapping("/onboarding/firm/details")
    public ResponseEntity<RestResponse<String>> completeFirmDetailsOnboarding(@Valid @RequestBody FirmOnboardingDetailsRequest request) {
        userService.completeFirmDetailsOnboarding(request);
        return ResponseEntity.ok(RestResponse.success("Firm onboarding completed"));
    }

    @PutMapping("/onboarding/supplier/basic")
    public ResponseEntity<RestResponse<String>> completeSupplierBasicOnboarding(@Valid @RequestBody SupplierOnboardingBasicRequest request) {
        userService.completeSupplierBasicOnboarding(request);
        return ResponseEntity.ok(RestResponse.success("Supplier basic onboarding completed"));
    }

    @PutMapping("/onboarding/supplier/capabilities")
    public ResponseEntity<RestResponse<String>> completeSupplierCapabilitiesOnboarding(@Valid @RequestBody SupplierOnboardingCapabilitiesRequest request) {
        userService.completeSupplierCapabilitiesOnboarding(request);
        return ResponseEntity.ok(RestResponse.success("Supplier capability onboarding completed"));
    }

    @PutMapping("/onboarding/supplier/qualifications")
    public ResponseEntity<RestResponse<String>> completeSupplierQualificationsOnboarding(@Valid @RequestBody SupplierOnboardingQualificationsRequest request) {
        userService.completeSupplierQualificationsOnboarding(request);
        return ResponseEntity.ok(RestResponse.success("Supplier onboarding completed"));
    }

    @PostMapping("/onboarding/website-suggestions")
    public ResponseEntity<RestResponse<WebsiteSuggestionResponse>> generateWebsiteSuggestions(@Valid @RequestBody WebsiteSuggestionRequest request) {
        return ResponseEntity.ok(RestResponse.success("Website suggestions generated", userService.generateWebsiteSuggestions(request)));
    }

    @Operation(summary = "Update firm profile", description = "Update the current authenticated firm's organization profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @PutMapping("/profile/firm")
    public ResponseEntity<RestResponse<String>> updateFirmProfile(
            @Parameter(description = "Firm profile update data", required = true) @Valid @RequestBody FirmProfileUpdateRequest request) {
        userService.updateFirmProfile(request);
        return ResponseEntity.ok(RestResponse.success("Firm profile updated successfully"));
    }

    @Operation(summary = "Update supplier profile", description = "Update the current authenticated supplier's organization profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @PutMapping("/profile/supplier")
    public ResponseEntity<RestResponse<String>> updateSupplierProfile(
            @Parameter(description = "Supplier profile update data", required = true) @Valid @RequestBody SupplierProfileUpdateRequest request) {
        userService.updateSupplierProfile(request);
        return ResponseEntity.ok(RestResponse.success("Supplier profile updated successfully"));
    }

    @PostMapping(value = "/profile/supplier/certificates/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<RestResponse<UploadedFileResponse>> uploadSupplierCertificate(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "displayName", required = false) String displayName) throws IOException {
        UploadedFileResponse response = userService.uploadSupplierCertificate(file, displayName);
        return ResponseEntity.ok(RestResponse.success("Supplier certificate uploaded successfully", response));
    }

    @DeleteMapping("/profile/supplier/certificates")
    public ResponseEntity<RestResponse<String>> deleteSupplierCertificate(
            @RequestParam("fileUrl") String fileUrl) {
        userService.deleteSupplierCertificate(fileUrl);
        return ResponseEntity.ok(RestResponse.success("Supplier certificate deleted successfully"));
    }

    @GetMapping("/profile/supplier/certificates/view")
    public ResponseEntity<Resource> viewSupplierCertificate(
            @RequestParam("fileUrl") String fileUrl) {
        FileResourceResponse response = userService.getSupplierCertificateResource(fileUrl);
        return ResponseEntity.ok()
                .headers(response.headers())
                .body(response.resource());
    }

    @Operation(summary = "Change password", description = "Change the current authenticated user's password. Requires current password verification.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or current password incorrect"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @PutMapping("/change-password")
    public ResponseEntity<RestResponse<String>> changePassword(
            @Parameter(description = "Password change data", required = true) @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(RestResponse.success("Password updated successfully"));
    }

    @Operation(summary = "Deactivate user account", description = "Deactivate the current authenticated user's account (soft delete). The account can be reactivated by an admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account deactivated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @DeleteMapping("/deactivate")
    public ResponseEntity<Void> deactivateUser() {
        userService.deactivateAuthenticatedUser();
        return ResponseEntity.noContent().build();
    }

}
