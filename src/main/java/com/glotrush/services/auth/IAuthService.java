package com.glotrush.services.auth;

import com.glotrush.dto.request.ForgotPasswordRequest;
import com.glotrush.dto.request.LoginRequest;
import com.glotrush.dto.request.RegisterRequest;
import com.glotrush.dto.request.ResetPasswordRequest;
import com.glotrush.dto.request.Verify2FARequest;
import com.glotrush.dto.response.LoginResponse;
import com.glotrush.dto.response.RefreshTokenResponse;
import com.glotrush.dto.response.RegisterResponse;

import jakarta.servlet.http.HttpServletResponse;

public interface IAuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request, HttpServletResponse response);

    LoginResponse verify2FA(Verify2FARequest request, HttpServletResponse response);

    RefreshTokenResponse refreshToken(String refreshToken, HttpServletResponse response);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void logout(String refreshToken, HttpServletResponse response);
}