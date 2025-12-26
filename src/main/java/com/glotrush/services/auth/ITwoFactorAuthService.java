package com.glotrush.services.auth;

import java.util.UUID;

import com.glotrush.dto.request.Enable2FARequest;
import com.glotrush.dto.request.Verify2FASetupRequest;
import com.glotrush.dto.response.Enable2FAResponse;

public interface ITwoFactorAuthService {

    Enable2FAResponse enable2FA(Enable2FARequest request, UUID userId);

    void verify2FASetup(Verify2FASetupRequest request, UUID userId);

    void disable2FA(UUID userId, String verificationCode);
}