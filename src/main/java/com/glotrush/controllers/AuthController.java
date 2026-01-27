package com.glotrush.controllers;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.glotrush.dto.request.Disable2FARequest;
import com.glotrush.dto.request.Enable2FARequest;
import com.glotrush.dto.request.ForgotPasswordRequest;
import com.glotrush.dto.request.LoginRequest;
import com.glotrush.dto.request.RegisterRequest;
import com.glotrush.dto.request.ResetPasswordRequest;
import com.glotrush.dto.request.Verify2FARequest;
import com.glotrush.dto.request.Verify2FASetupRequest;
import com.glotrush.dto.response.ApiResponse;
import com.glotrush.dto.response.Enable2FAResponse;
import com.glotrush.dto.response.LoginResponse;
import com.glotrush.dto.response.RefreshTokenResponse;
import com.glotrush.dto.response.RegisterResponse;
import com.glotrush.dto.response.UserInfoResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.TwoFactorAuthRepository;
import com.glotrush.services.auth.IAuthService;
import com.glotrush.services.auth.ITwoFactorAuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final MessageSource messageSource;
    private final IAuthService authService;
    private final ITwoFactorAuthService twoFactorAuthService;
    private final AccountsRepository accountsRepository;
    private final TwoFactorAuthRepository twoFactorAuthRepository;
    

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request){
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
        @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request, response);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<LoginResponse> verify2FA(@Valid @RequestBody Verify2FARequest request,HttpServletResponse response) {
        LoginResponse loginResponse = authService.verify2FA(request, response);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(HttpServletRequest request,HttpServletResponse response, Locale locale) {
        String refreshToken = extractRefreshTokenFromCookie(request ,locale);
        RefreshTokenResponse tokenResponse = authService.refreshToken(refreshToken, response);
        return ResponseEntity.ok(tokenResponse);
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(new ApiResponse(messageSource.getMessage("success.auth.password_reset_sent", null, null)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse(messageSource.getMessage("success.auth.password_reset_success", null, null)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request,HttpServletResponse response, Locale locale) {
        String refreshToken = extractRefreshTokenFromCookie(request, locale);
        authService.logout(refreshToken, response);
        return ResponseEntity.ok(new ApiResponse(messageSource.getMessage("success.auth.logout", null, locale)));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<Enable2FAResponse> enable2FA(@Valid @RequestBody Enable2FARequest request,Authentication authentication) {
        UUID accountId = extractUserIdFromAuth(authentication);
        Enable2FAResponse response = twoFactorAuthService.enable2FA(request, accountId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/verify-setup")
    public ResponseEntity<ApiResponse> verify2FASetup(@Valid @RequestBody Verify2FASetupRequest request,Authentication authentication, Locale locale) {
        UUID accountId = extractUserIdFromAuth(authentication);
        twoFactorAuthService.verify2FASetup(request, accountId);
        return ResponseEntity.ok(new ApiResponse(messageSource.getMessage("success.2fa.enabled", null, locale)));
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<ApiResponse> disable2FA(@Valid @RequestBody Disable2FARequest request, Authentication authentication, Locale locale) {
        UUID accountId = extractUserIdFromAuth(authentication);
        twoFactorAuthService.disable2FA(accountId, request.getCode());
        return ResponseEntity.ok(new ApiResponse(messageSource.getMessage("success.2fa.disabled", null, locale)));
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(Authentication authentication, Locale locale) {
        UUID accountId = extractUserIdFromAuth(authentication);
        Accounts account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException(messageSource.getMessage("error.auth.account_not_found", null, locale)));
        
        boolean has2FA = twoFactorAuthRepository.existsByAccount_IdAndActiveTrue(accountId);
        
        return ResponseEntity.ok(UserInfoResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .username(account.getUsername())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .role(account.getRole().name())
                .has2FAEnabled(has2FA)
                .build());
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request, Locale locale) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> "refresh_token".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(messageSource.getMessage("error.auth.refresh_token_not_found", null, locale)));
        }
        throw new RuntimeException(messageSource.getMessage("error.auth.no_cookies_found", null, locale));
    }

    private UUID extractUserIdFromAuth(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return UUID.fromString(userId);
    }
}
