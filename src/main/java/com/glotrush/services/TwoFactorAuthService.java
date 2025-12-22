package com.glotrush.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.glotrush.builder.TwoFactorAuthBuilder;
import com.glotrush.dto.request.Enable2FARequest;
import com.glotrush.dto.request.Verify2FASetupRequest;
import com.glotrush.dto.response.Enable2FAResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.TwoFactorAuth;
import com.glotrush.exceptions.InvalidTotpCodeException;
import com.glotrush.exceptions.TwoFactorAlreadyEnabledException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.TwoFactorAuthRepository;
import com.glotrush.security.totp.TotpService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorAuthService {

    private final TwoFactorAuthRepository twoFactorAuthRepository;
    private final AccountsRepository accountsRepository;
    private final TotpService totpService;
    private final TwoFactorAuthBuilder twoFactorAuthBuilder;


    @Transactional
    public Enable2FAResponse enable2FA(Enable2FARequest request, UUID userId) {
        Accounts account = accountsRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Account not found"));

        if (twoFactorAuthRepository.existsByUserIdAndIsActiveTrue(userId)) {
            throw new TwoFactorAlreadyEnabledException("2FA is already enabled");
        }

        String secret = totpService.generateSecret();
        String encryptedSecret = totpService.encryptSecret(secret);

        TwoFactorAuth twoFactorAuth = twoFactorAuthBuilder.buildTwoFactorAuth(account, encryptedSecret);

        TwoFactorAuth saved = twoFactorAuthRepository.save(twoFactorAuth);

        String qrCodeUri = totpService.generateQrCodeImageUri(secret, account.getEmail(), account.getUsername());

        log.info("2FA setup initiated for account with this email: {}", account.getEmail());

        return Enable2FAResponse.builder()
                .secret(secret)
                .qrCodeUri(qrCodeUri)
                .twoFactorAuthId(saved.getId())
                .message("Scan the QR code with Google Authenticator and verify with a code")
                .build();
    }

    @Transactional
    public void verify2FASetup(Verify2FASetupRequest request, UUID userId) {
        Accounts account = accountsRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Account not found"));

        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findById(request.getTwoFactorAuthId())
                .filter(tfa -> tfa.getAccount().getId().equals(userId) && !tfa.getActive())
                .orElseThrow(() -> new RuntimeException("2FA setup not found or already active"));

        String decryptedSecret = totpService.decryptSecret(twoFactorAuth.getSecret());
        boolean isValid = totpService.verifyCode(decryptedSecret, request.getCode());

        if (!isValid) {
            throw new InvalidTotpCodeException("Invalid verification code");
        }

        twoFactorAuth.setActive(true);
        twoFactorAuth.setActivatedAt(LocalDateTime.now());
        twoFactorAuthRepository.save(twoFactorAuth);

        log.info("2FA successfully enabled for account: {}", account.getEmail());
    }

    @Transactional
    public void disable2FA(UUID userId, String verificationCode) {
        Accounts account = accountsRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Account not found"));

        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findFirstByAccountIdAndActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("2FA is not enabled"));

        String decryptedSecret = totpService.decryptSecret(twoFactorAuth.getSecret());
        boolean isValid = totpService.verifyCode(decryptedSecret, verificationCode);

        if (!isValid) {
            throw new InvalidTotpCodeException("Invalid verification code");
        }

        twoFactorAuthRepository.delete(twoFactorAuth);

        log.info("2FA disabled for account: {}", account.getEmail());
    }
}