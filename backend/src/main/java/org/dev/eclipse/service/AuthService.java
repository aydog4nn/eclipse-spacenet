package org.dev.eclipse.service;

import jakarta.servlet.http.HttpServletResponse;
import org.dev.eclipse.dto.request.FirmRegisterRequest;
import org.dev.eclipse.dto.request.ForgotPasswordRequest;
import org.dev.eclipse.dto.request.ResetPasswordRequest;
import org.dev.eclipse.dto.request.SupplierRegisterRequest;
import org.dev.eclipse.dto.request.UserLoginRequest;
import org.dev.eclipse.dto.response.AuthResponse;
import org.dev.eclipse.dto.response.UserResponse;

public interface AuthService {

    UserResponse registerFirm(FirmRegisterRequest request);

    UserResponse registerSupplier(SupplierRegisterRequest request);

    AuthResponse login(UserLoginRequest request, HttpServletResponse response);

    AuthResponse refreshToken(String refreshToken, String csrfToken);

    void sendResetPasswordCode(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void logout(String authorizationHeader, String refreshToken, String csrfToken, HttpServletResponse response);

}
