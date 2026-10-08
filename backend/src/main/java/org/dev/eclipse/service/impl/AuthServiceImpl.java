package org.dev.eclipse.service.impl;

import io.jsonwebtoken.Claims;
import org.dev.eclipse.annotation.Audit;
import org.dev.eclipse.dto.mapper.UserMapper;
import org.dev.eclipse.dto.request.*;
import org.dev.eclipse.dto.request.FirmRegisterRequest;
import org.dev.eclipse.dto.response.AuthResponse;
import org.dev.eclipse.dto.response.UserResponse;
import org.dev.eclipse.entity.PasswordResetToken;
import org.dev.eclipse.entity.User;
import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.exception.AuthExceptions.AuthException;
import org.dev.eclipse.repository.PasswordResetTokenRepository;
import org.dev.eclipse.repository.UserRepository;
import org.dev.eclipse.security.JwtService;
import org.dev.eclipse.service.*;
import org.dev.eclipse.service.async.AsyncMailDispatcher;
import org.dev.eclipse.util.CodeGeneratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@Validated
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AsyncMailDispatcher asyncMailDispatcher;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginAttemptService loginAttemptService;
    private final OrganizationSearchService organizationSearchService;
    private final UserMapper userMapper;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.csrf-token-expiration}")
    private long csrfTokenExpiration;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.verification.token-length}")
    private int tokenLength;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService, AsyncMailDispatcher asyncMailDispatcher,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           TokenBlacklistService tokenBlacklistService,
                           LoginAttemptService loginAttemptService,
                           OrganizationSearchService organizationSearchService,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.asyncMailDispatcher = asyncMailDispatcher;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.tokenBlacklistService = tokenBlacklistService;
        this.loginAttemptService = loginAttemptService;
        this.organizationSearchService = organizationSearchService;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    @Audit(action = "REGISTER_FIRM", resource = "User")
    public UserResponse registerFirm(FirmRegisterRequest request) {
        logger.info("Register request received for email: {}", request.email());

        FirmRegisterRequest normalizedRequest = normalizeFirmRequest(request);
        validateEmailAndUsernameUniqueness(normalizedRequest.email(), normalizedRequest.username());

        User user = userMapper.toFirmEntity(normalizedRequest);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setActive(true);

        userRepository.saveAndFlush(user);
        organizationSearchService.indexOrganization(user.getOrganization());

        asyncMailDispatcher.sendWelcomeEmail(user.getEmail(), normalizedRequest.organizationName());

        logger.info("Firm user registered and awaiting admin approval: {}", request.email());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    @Audit(action = "REGISTER_SUPPLIER", resource = "User")
    public UserResponse registerSupplier(SupplierRegisterRequest request) {
        logger.info("Supplier register request received for email: {}", request.email());

        SupplierRegisterRequest normalizedRequest = normalizeSupplierRequest(request);
        validateEmailAndUsernameUniqueness(normalizedRequest.email(), normalizedRequest.username());

        User user = userMapper.toSupplierEntity(normalizedRequest);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setActive(true);

        userRepository.saveAndFlush(user);
        organizationSearchService.indexOrganization(user.getOrganization());

        asyncMailDispatcher.sendWelcomeEmail(user.getEmail(), normalizedRequest.organizationName());

        logger.info("Supplier user registered successfully: {}", request.email());
        return userMapper.toResponse(user);
    }

    @Override
    @Audit(action = "LOGIN", resource = "User")
    public AuthResponse login(UserLoginRequest request, HttpServletResponse response) {
        String email = request.email();
        logger.info("Login attempt for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Login failed. Email not found: {}", email);
                    return new AuthException("Invalid email or password");
                });

        if (!user.isActive()) {
            logger.warn("Login failed. User inactive: {}", email);
            throw new AuthException("User is inactive");
        }

        if ((user.getRole() == Role.FIRMA || user.getRole() == Role.TEDARIKCI)
                && (user.getOrganization() == null || !user.getOrganization().isApproved())) {
            logger.warn("Login failed. Organization approval pending: {}", email);
            throw new AuthException("Hesabiniz admin onayi bekliyor.");
        }


        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            logger.warn("Login failed. Invalid password for email: {}", email);
            throw new AuthException("Invalid email or password");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        String csrfToken = jwtService.generateCsrfToken(user.getEmail());

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshTokenExpiration / 1000));
        response.addCookie(refreshTokenCookie);

        Cookie csrfTokenCookie = new Cookie("csrf_token", csrfToken);
        csrfTokenCookie.setHttpOnly(false);
        csrfTokenCookie.setSecure(true);
        csrfTokenCookie.setPath("/");
        csrfTokenCookie.setMaxAge((int) (csrfTokenExpiration / 1000));
        response.addCookie(csrfTokenCookie);

        logger.info("Login successful for email: {}", email);

        return new AuthResponse(accessToken);
    }

    @Override
    @Audit(action = "REFRESH_TOKEN", resource = "User")
    public AuthResponse refreshToken(String refreshToken, String csrfToken) {
        logger.info("Refresh token request received");

        jwtService.validateTokenType(refreshToken, "refresh");
        if (tokenBlacklistService.isTokenBlacklisted(refreshToken) || tokenBlacklistService.isTokenBlacklisted(csrfToken)) {
            throw new AuthException("Token geçersiz");
        }
        Claims refreshClaims = jwtService.extractAllClaims(refreshToken);

        jwtService.validateTokenType(csrfToken, "csrf");
        Claims csrfClaims = jwtService.extractAllClaims(csrfToken);

        String refreshEmail = refreshClaims.getSubject();
        String csrfEmail = csrfClaims.getSubject();

        if (!constantTimeEquals(refreshEmail, csrfEmail)) {
            logger.warn("CSRF token does not match the refresh token");
            throw new AuthException("CSRF token does not match the refresh token");
        }

        Date expiration = refreshClaims.getExpiration();
        long now = System.currentTimeMillis();

        if (expiration == null || expiration.getTime() < now) {
            logger.warn("Refresh token expired");
            throw new AuthException("Refresh token expired");
        }

        User user = userRepository.findByEmail(refreshEmail)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", refreshEmail);
                    return new AuthException("User not found");
                });

        if (!user.isActive()) {
            logger.warn("User inactive: {}", refreshEmail);
            throw new AuthException("User is inactive");
        }

        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());

        logger.info("New access token generated for email: {}", refreshEmail);

        return new AuthResponse(newAccessToken);
    }


    @Override
    @Transactional
    @Audit(action = "SEND_RESET_PASSWORD_CODE", resource = "User")
    public void sendResetPasswordCode(ForgotPasswordRequest request) {
        String email = request.email();
        logger.info("Password reset code request received for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Password reset failed. Email not found: {}", email);
                    return new AuthException("Email address is not registered");
                });

        if (!user.isActive()) {
            logger.warn("Password reset failed. User is inactive: {}", email);
            throw new AuthException("Password reset failed. User is inactive.");
        }

        String code = CodeGeneratorUtil.generateSafeToken(tokenLength);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        passwordResetTokenRepository.deleteByUser(user);

        PasswordResetToken resetToken = new PasswordResetToken(code, user, expiresAt);
        passwordResetTokenRepository.save(resetToken);

        asyncMailDispatcher.sendResetPasswordEmail(email, code);

        logger.info("Password reset code sent to {}", email);
    }


    @Override
    @Transactional
    @Audit(action = "RESET_PASSWORD", resource = "User")
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new AuthException("Passwords do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new AuthException("Invalid or expired password reset code"));

        if (resetToken.isUsed()) {
            throw new AuthException("This code has already been used.");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthException("The code has expired.");
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        logger.info("Password reset successful: {}", user.getEmail());
    }

    @Override
    @Audit(action = "LOGOUT", resource = "User")
    public void logout(String authorizationHeader, String refreshToken, String csrfToken, HttpServletResponse response) {
        blacklistAccessToken(authorizationHeader);
        blacklistTokenAndClearCookie(refreshToken, "refresh_token", true, response);
        blacklistTokenAndClearCookie(csrfToken, "csrf_token", false, response);
    }

    private void validateEmailAndUsernameUniqueness(String email, String username) {
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            throw new AuthException("Email is already registered");
        });

        userRepository.findByUsername(username).ifPresent(existingUser -> {
            throw new AuthException("Username is already registered");
        });
    }

    private FirmRegisterRequest normalizeFirmRequest(FirmRegisterRequest request) {
        return new FirmRegisterRequest(
                request.organizationName().trim(),
                request.username().trim(),
                request.email().trim(),
                request.password(),
                request.phoneNumber().trim(),
                trimToNull(request.location()),
                trimToNull(request.website())
        );
    }

    private SupplierRegisterRequest normalizeSupplierRequest(SupplierRegisterRequest request) {
        List<String> normalizedFocusAreas = request.focusAreas().stream()
                .map(String::trim)
                .filter(area -> !area.isBlank())
                .distinct()
                .toList();

        if (normalizedFocusAreas.isEmpty()) {
            throw new AuthException("At least one valid focus area is required");
        }

        return new SupplierRegisterRequest(
                request.organizationName().trim(),
                request.username().trim(),
                request.email().trim(),
                request.password(),
                request.phoneNumber().trim(),
                request.supplierCategory(),
                trimToNull(request.location()),
                trimToNull(request.website()),
                normalizedFocusAreas
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void blacklistAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) return;

        String accessToken = authorizationHeader.substring(7);
        if (accessToken.isBlank()) return;

        long expirationMillis = getTokenTTL(accessToken);
        if (expirationMillis > 0) {
            tokenBlacklistService.blacklistToken(accessToken, expirationMillis);
        }
    }

    private void blacklistTokenAndClearCookie(String token, String cookieName, boolean httpOnly, HttpServletResponse response) {
        if (token == null || token.isBlank()) return;

        long expirationMillis = getTokenTTL(token);
        if (expirationMillis > 0) {
            tokenBlacklistService.blacklistToken(token, expirationMillis);
        }

        ResponseCookie deleteCookie = ResponseCookie.from(cookieName, "")
                .httpOnly(httpOnly)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", deleteCookie.toString());
    }

    private long getTokenTTL(String token) {
        try {
            Date expiration = jwtService.extractAllClaims(token).getExpiration();
            if (expiration == null) return 0L;

            long now = System.currentTimeMillis();
            long exp = expiration.getTime();
            return exp > now ? exp - now : 0L;
        } catch (Exception e) {
            logger.warn("getTokenTTL exception: {}", e.getMessage());
            return 0L;
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

}
