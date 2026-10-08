package org.dev.eclipse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.dev.eclipse.dto.request.ChangeUserRoleRequest;
import org.dev.eclipse.dto.request.FirmRegisterRequest;
import org.dev.eclipse.dto.request.OrganizationApprovalUpdateRequest;
import org.dev.eclipse.dto.request.UserSearchRequest;
import org.dev.eclipse.dto.response.RestResponse;
import org.dev.eclipse.dto.response.UserResponse;
import org.dev.eclipse.service.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Management", description = "Administrative user management endpoints")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Operation(summary = "Get all users", description = "Retrieve a list of all users in the system. Only accessible by ADMIN users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All users retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges")
    })
    @GetMapping("/users")
    public ResponseEntity<RestResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(RestResponse.success("All users listed", users));
    }

    @Operation(summary = "Hard delete user", description = "Permanently delete a user from the system. This action cannot be undone. Only accessible by ADMIN users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> hardDeleteUser(
            @Parameter(description = "ID of the user to delete", required = true, example = "1") @PathVariable UUID userId) {
        adminUserService.hardDeleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Register new user", description = "Register a new user account. Access becomes available after admin approval.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<RestResponse<UserResponse>> register(
            @Parameter(description = "Firm registration data", required = true) @Valid @RequestBody FirmRegisterRequest request) {
        UserResponse userResponse = adminUserService.register(request);
        return ResponseEntity.ok(RestResponse.success("Registration successful", userResponse));
    }

    @Operation(summary = "Change user role", description = "Change the role of a specific user. Only accessible by ADMIN users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User role changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<RestResponse<String>> changeUserRole(
            @Parameter(description = "ID of the user to change role", required = true, example = "1") @PathVariable UUID userId,
            @Parameter(description = "New role information", required = true) @Valid @RequestBody ChangeUserRoleRequest request) {
        adminUserService.changeUserRole(userId, request);
        return ResponseEntity.ok(RestResponse.success("User role changed successfully"));
    }

    @PatchMapping("/users/{userId}/approval")
    public ResponseEntity<RestResponse<String>> updateOrganizationApproval(
            @PathVariable UUID userId,
            @Valid @RequestBody OrganizationApprovalUpdateRequest request) {
        adminUserService.updateOrganizationApproval(userId, request);
        return ResponseEntity.ok(RestResponse.success("Organization approval updated successfully"));
    }

    @Operation(summary = "Search users", description = "Search users by various criteria (name, email, role, etc.). Only accessible by ADMIN users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges")
    })
    @GetMapping("/users/search")
    public ResponseEntity<RestResponse<List<UserResponse>>> searchUsers(
            @Parameter(description = "Search criteria (name, email, role, etc.)") @ModelAttribute UserSearchRequest request) {
        List<UserResponse> users = adminUserService.searchUsers(request);
        return ResponseEntity.ok(RestResponse.success("Search results listed", users));
    }

}
