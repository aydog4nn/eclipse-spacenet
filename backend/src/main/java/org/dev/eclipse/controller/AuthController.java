package org.dev.eclipse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.dev.eclipse.dto.request.*;
import org.dev.eclipse.dto.response.AuthResponse;
import org.dev.eclipse.dto.response.RestResponse;
import org.dev.eclipse.dto.response.UserResponse;
import org.dev.eclipse.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "User login", description = "Authenticate user and return JWT access token. Refresh token and CSRF token will be set as cookies.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or account blocked", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<RestResponse<AuthResponse>> login(
            @Parameter(description = "User login credentials", required = true) @Valid @RequestBody UserLoginRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(RestResponse.success("Login successful", authResponse));
    }

    @Operation(summary = "Register new user", description = "Register a new firm account. Account access becomes available after admin approval.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<RestResponse<UserResponse>> registerFirm(
            @Parameter(description = "Firm registration data", required = true) @Valid @RequestBody FirmRegisterRequest request) {
        UserResponse userResponse = authService.registerFirm(request);
        return ResponseEntity.ok(RestResponse.success("Firm registration successful", userResponse));
    }

    @Operation(summary = "Register new supplier", description = "Register a new supplier account with organization details and supplier category.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier registered successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data, email exists, or username exists")
    })
    @PostMapping("/register/supplier")
    public ResponseEntity<RestResponse<UserResponse>> registerSupplier(
            @Parameter(description = "Supplier registration data", required = true) @Valid @RequestBody SupplierRegisterRequest request) {
        UserResponse userResponse = authService.registerSupplier(request);
        return ResponseEntity.ok(RestResponse.success("Supplier registration successful", userResponse));
    }

    @Operation(summary = "Refresh access token", description = "Get a new access token using refresh token. Requires CSRF token validation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid CSRF token"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<RestResponse<AuthResponse>> refreshToken(
            @Parameter(description = "Refresh token from cookie", required = true) @CookieValue("refresh_token") String refreshToken,
            @Parameter(description = "CSRF token from cookie", required = true) @CookieValue("csrf_token") String csrfCookie,
            @Parameter(description = "CSRF token from header", required = true) @RequestHeader("X-CSRF-TOKEN") String csrfHeader) {

        if (!MessageDigest.isEqual(csrfCookie.getBytes(), csrfHeader.getBytes())) {
            return ResponseEntity
                    .badRequest()
                    .body(RestResponse.fail("Invalid CSRF token"));
        }

        AuthResponse authResponse = authService.refreshToken(refreshToken, csrfCookie);
        return ResponseEntity.ok(RestResponse.success("Token refreshed", authResponse));
    }

    @Operation(summary = "Request password reset", description = "Send password reset code to user's email address. Rate limited to 3 requests per 24 hours.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset code sent successfully"),
            @ApiResponse(responseCode = "400", description = "Email not found or user inactive"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<RestResponse<String>> forgotPassword(
            @Parameter(description = "Email address for password reset", required = true) @Valid @RequestBody ForgotPasswordRequest request) {
        authService.sendResetPasswordCode(request);
        return ResponseEntity.ok(RestResponse.success("Password reset code sent to your email address."));
    }

    @Operation(summary = "Reset password", description = "Reset user password using the password reset code sent via email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid code or passwords don't match")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<RestResponse<String>> resetPassword(
            @Parameter(description = "Password reset data", required = true) @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(RestResponse.success("Password has been reset successfully"));
    }

    @Operation(summary = "User logout", description = "Logout user and invalidate all tokens. Clears cookies and blacklists tokens.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/logout")
    public ResponseEntity<RestResponse<String>> logout(
            @Parameter(description = "Authorization header with Bearer token") @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "Refresh token from cookie") @CookieValue(value = "refresh_token", required = false) String refreshToken,
            @Parameter(description = "CSRF token from cookie") @CookieValue(value = "csrf_token", required = false) String csrfToken,
            HttpServletResponse response) {
        authService.logout(authorizationHeader, refreshToken, csrfToken, response);
        return ResponseEntity.ok(RestResponse.success("Successfully logged out"));
    }
}
