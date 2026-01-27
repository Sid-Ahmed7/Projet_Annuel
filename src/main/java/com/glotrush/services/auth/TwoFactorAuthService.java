package com.glotrush.services.auth;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
public class TwoFactorAuthService implements ITwoFactorAuthService {
    private final MessageSource messageSource;
    private final TwoFactorAuthRepository twoFactorAuthRepository;
    private final AccountsRepository accountsRepository;
    private final TotpService totpService;
    private final TwoFactorAuthBuilder twoFactorAuthBuilder;

    protected final Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    @Transactional
    public Enable2FAResponse enable2FA(Enable2FARequest request, UUID userId) {
        Accounts account = accountsRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, getCurrentLocale())));

        if (twoFactorAuthRepository.existsByAccount_IdAndActiveTrue(userId)) {
            throw new TwoFactorAlreadyEnabledException(messageSource.getMessage("error.2fa.already_enabled", null, getCurrentLocale()));
        }

        String secret = totpService.generateSecret();
        String encryptedSecret = totpService.encryptSecret(secret);

        TwoFactorAuth twoFactorAuth = twoFactorAuthBuilder.buildTwoFactorAuth(account, encryptedSecret);

        TwoFactorAuth saved = twoFactorAuthRepository.save(twoFactorAuth);

        String issuer = account.getUsername() != null ? account.getUsername() : "Glotrush";
        String qrCodeUri = totpService.generateQrCodeImageUri(secret, account.getEmail(), issuer);

        log.info("2FA setup initiated for account with this email: {}", account.getEmail());

        return Enable2FAResponse.builder()
                .secret(secret)
                .qrCodeUri(qrCodeUri)
                .twoFactorAuthId(saved.getId())
                .message(messageSource.getMessage("info.2fa.scan_qr", null, getCurrentLocale()))
                .build();
    }

    @Transactional
    public void verify2FASetup(Verify2FASetupRequest request, UUID userId) {
        Accounts account = accountsRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, getCurrentLocale())));

        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findById(request.getTwoFactorAuthId())
                .filter(tfa -> tfa.getAccount().getId().equals(userId) && !tfa.getActive())
                .orElseThrow(() -> new RuntimeException(messageSource.getMessage("error.2fa.setup_not_found", null, getCurrentLocale())));

        String decryptedSecret = totpService.decryptSecret(twoFactorAuth.getSecret());
        boolean isValid = totpService.verifyCode(decryptedSecret, request.getCode());

        if (!isValid) {
            throw new InvalidTotpCodeException(messageSource.getMessage("error.2fa.invalid_code", null, getCurrentLocale()));
        }

        twoFactorAuth.setActive(true);
        twoFactorAuth.setActivatedAt(LocalDateTime.now());
        twoFactorAuthRepository.save(twoFactorAuth);

        log.info("2FA successfully enabled for account: {}", account.getEmail());
    }

    @Transactional
    public void disable2FA(UUID userId, String verificationCode) {
        Accounts account = accountsRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, getCurrentLocale())));

        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findFirstByAccount_IdAndActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException(messageSource.getMessage("error.2fa.not_enabled", null, getCurrentLocale())));

        String decryptedSecret = totpService.decryptSecret(twoFactorAuth.getSecret());
        boolean isValid = totpService.verifyCode(decryptedSecret, verificationCode);

        if (!isValid) {
            throw new InvalidTotpCodeException(messageSource.getMessage("error.2fa.invalid_code", null, getCurrentLocale()));
        }

        twoFactorAuthRepository.delete(twoFactorAuth);

        log.info("2FA disabled for account: {}", account.getEmail());
    }
}