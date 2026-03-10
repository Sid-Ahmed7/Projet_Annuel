package com.glotrush.services.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.glotrush.builder.AccountBuilder;
import com.glotrush.builder.RefreshTokenBuilder;
import com.glotrush.dto.request.AdminLoginRequest;
import com.glotrush.dto.request.ForgotPasswordRequest;
import com.glotrush.dto.request.LoginRequest;
import com.glotrush.dto.request.RegisterRequest;
import com.glotrush.dto.request.ResetPasswordRequest;
import com.glotrush.dto.request.Verify2FARequest;
import com.glotrush.dto.response.LoginResponse;
import com.glotrush.dto.response.RefreshTokenResponse;
import com.glotrush.dto.response.RegisterResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.PasswordResetToken;
import com.glotrush.entities.RefreshToken;
import com.glotrush.entities.TwoFactorAuth;
import com.glotrush.enumerations.UserRole;
import com.glotrush.exceptions.AccountLockedException;
import com.glotrush.exceptions.EmailAlreadyExistsException;
import com.glotrush.exceptions.InvalidTokenException;
import com.glotrush.exceptions.InvalidTotpCodeException;
import com.glotrush.exceptions.PasswordExpiredException;
import com.glotrush.exceptions.TwoFactorNotEnabledException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.exceptions.UsernameAlreadyExistsException;
import com.glotrush.exceptions.WeakPasswordException;
import org.springframework.security.access.AccessDeniedException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.PasswordResetTokenRepository;
import com.glotrush.repositories.RefreshTokenRepository;
import com.glotrush.repositories.TwoFactorAuthRepository;
import com.glotrush.security.jwt.JwtService;
import com.glotrush.security.totp.TotpService;
import com.glotrush.services.EmailService;
import com.glotrush.services.subscription.ISubscriptionService;
import com.glotrush.utils.LocaleUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {
    private final MessageSource messageSource;
    private final AccountsRepository accountsRepository;
    private final TwoFactorAuthRepository twoFactorAuthRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final AccountBuilder accountBuilder;
    private final RefreshTokenBuilder refreshTokenBuilder;
    private final ISubscriptionService subscriptionService;
    private final LoginAttemptService loginAttemptService;
    private final TotpService totpService;


    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private static final int PASSWORD_EXPIRY_DAYS = 60;


    @Transactional
    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (accountsRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(messageSource.getMessage("error.auth.email_already_registered", null, LocaleUtils.getCurrentLocale()));
        }

        if (request.getUsername() != null && accountsRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(messageSource.getMessage("error.auth.username_already_taken", null, LocaleUtils.getCurrentLocale()));
        }

        validatePasswordStrength(request.getPassword());

        Accounts account = accountBuilder.buildAccount(request);

        Accounts savedAccount = accountsRepository.save(account);
        log.info("User registered successfully: {}", savedAccount.getEmail());

        subscriptionService.createSubscriptionForUser(savedAccount);
        return accountBuilder.buildRegisterResponse(savedAccount);
    }

    @Transactional
    @Override
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        Accounts account = accountsRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException(messageSource.getMessage("error.auth.invalid_credentials", null, LocaleUtils.getCurrentLocale())));

        checkAccountLock(account);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            loginAttemptService.resetFailedLoginAttempts(account);
            checkPasswordExpiry(account);

            boolean requires2FA = twoFactorAuthRepository.existsByAccount_IdAndActiveTrue(account.getId());

            if (requires2FA) {
                return LoginResponse.builder()
                        .requires2FA(true)
                        .tempUserId(account.getId().toString())
                        .message(messageSource.getMessage("success.2fa.code_required", null, LocaleUtils.getCurrentLocale()))
                        .build();
            }

            return generateAuthTokens(account, response);

        } catch (BadCredentialsException e) {
            boolean locked = loginAttemptService.handleFailedLogin(account);
            if (locked) {
                throw new AccountLockedException(messageSource.getMessage("error.auth.account_locked_attempts", null, LocaleUtils.getCurrentLocale()));
            }
            throw new BadCredentialsException(messageSource.getMessage("error.auth.invalid_credentials", null, LocaleUtils.getCurrentLocale()));
        }
    }

    @Transactional
    @Override
    public LoginResponse adminLogin(AdminLoginRequest request, HttpServletResponse response) {
        Accounts account = accountsRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException(messageSource.getMessage("error.auth.invalid_credentials", null, LocaleUtils.getCurrentLocale())));

        checkAccountLock(account);

        if (account.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException(messageSource.getMessage("error.access_required", null, LocaleUtils.getCurrentLocale()));
        }

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new BadCredentialsException(messageSource.getMessage("error.auth.invalid_credentials", null, LocaleUtils.getCurrentLocale()));
        }

        if (account.getAuthKey() == null ||
            !passwordEncoder.matches(request.getSecretKey(), account.getAuthKey())) {
            throw new BadCredentialsException(messageSource.getMessage("error.auth.invalid_credentials", null, LocaleUtils.getCurrentLocale()));
        }

        loginAttemptService.resetFailedLoginAttempts(account);

        return generateAuthTokens(account, response);
    }

    @Transactional
    @Override
    public LoginResponse verify2FA(Verify2FARequest request, HttpServletResponse response) {
        UUID userId = UUID.fromString(request.getTempUserId());
        Accounts account = accountsRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, LocaleUtils.getCurrentLocale())));

        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findFirstByAccount_IdAndActiveTrue(userId)
                .orElseThrow(() -> new TwoFactorNotEnabledException(messageSource.getMessage("error.2fa.not_enabled", null, LocaleUtils.getCurrentLocale())));
        String decrtypt = totpService.decryptSecret(twoFactorAuth.getSecret());
        boolean isValid = totpService.verifyCode(decrtypt, request.getCode());
       
        if(!isValid) {
            throw new InvalidTotpCodeException(messageSource.getMessage("error.2fa.invalid_code", null, LocaleUtils.getCurrentLocale()));
        }

        twoFactorAuth.setLastUsedAt(LocalDateTime.now());
        twoFactorAuthRepository.save(twoFactorAuth);

        return generateAuthTokens(account, response);
    }

    @Transactional
    @Override
    public RefreshTokenResponse refreshToken(String refreshToken, HttpServletResponse response) {
        RefreshToken token = refreshTokenRepository.findValidToken(refreshToken, LocalDateTime.now())
                .orElseThrow(() -> new InvalidTokenException(messageSource.getMessage("error.auth.invalid_token", null, LocaleUtils.getCurrentLocale())));

        Accounts user = token.getAccount();
        
        String newAccessToken = jwtService.generateAccessToken(
            user.getId().toString(),
            user.getRole().name()
        );

        String newRefreshToken = jwtService.generateRefreshToken(
            user.getId().toString()
        );

        token.setIsRevoked(true);
        refreshTokenRepository.save(token);

        createRefreshToken(user, newRefreshToken, null);
        setAuthCookies(response, newAccessToken, newRefreshToken);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .message(messageSource.getMessage("success.auth.tokens_refreshed", null, LocaleUtils.getCurrentLocale()))
                .build();
    }

    @Transactional
    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        Accounts account = accountsRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, LocaleUtils.getCurrentLocale())));

        passwordResetTokenRepository.invalidateAllUserTokens(account.getId());

        String token = UUID.randomUUID().toString();
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .account(account)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(account.getEmail(), resetLink);

    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findValidToken(request.getToken(), LocalDateTime.now()).orElseThrow(() -> new InvalidTokenException(messageSource.getMessage("error.auth.invalid_reset_token", null, LocaleUtils.getCurrentLocale())));

        Accounts account = resetToken.getAccount();

        validatePasswordStrength(request.getNewPassword());

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        account.setLastPasswordChange(LocalDateTime.now());
        accountsRepository.save(account);

        resetToken.setIsUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.revokeAllUserTokens(account.getId(), LocalDateTime.now());

    }

    @Transactional
    @Override
    public void logout(String refreshToken, HttpServletResponse response) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setIsRevoked(true);
                    refreshTokenRepository.save(token);
                });

        clearAuthCookies(response);
    }

    private LoginResponse generateAuthTokens(Accounts user, HttpServletResponse response) {
        String accessToken = jwtService.generateAccessToken(
            user.getId().toString(), 
            user.getRole().name()
        );
        String refreshToken = jwtService.generateRefreshToken(
            user.getId().toString()
        );

        createRefreshToken(user, refreshToken, null);
        setAuthCookies(response, accessToken, refreshToken);

        return accountBuilder.buildLoginResponse(user, accessToken, refreshToken, false);
    }

    private RefreshToken createRefreshToken(Accounts account, String token, String deviceInfo) {
        RefreshToken refreshToken = refreshTokenBuilder.buildRefreshToken(account, token, deviceInfo, refreshTokenExpiration);

        return refreshTokenRepository.save(refreshToken);
    }

    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(15 * 60);
        accessTokenCookie.setAttribute("SameSite", "Lax");

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);
        refreshTokenCookie.setAttribute("SameSite", "Lax");

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    private void clearAuthCookies(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("access_token", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);

        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    private void checkAccountLock(Accounts account) {
        if (account.getAccountLockedUntil() != null && account.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            throw new AccountLockedException(messageSource.getMessage("error.auth.account_locked_until", null, LocaleUtils.getCurrentLocale()) + account.getAccountLockedUntil());
        }
    }

    private void checkPasswordExpiry(Accounts account) {
        LocalDateTime expiryDate = account.getLastPasswordChange().plusDays(PASSWORD_EXPIRY_DAYS);
        if (LocalDateTime.now().isAfter(expiryDate)) {
            throw new PasswordExpiredException(messageSource.getMessage("error.auth.password_expired", null, LocaleUtils.getCurrentLocale()));
        }
    }

    private void validatePasswordStrength(String password) {
        if (password.length() < 12) {
            throw new WeakPasswordException(messageSource.getMessage("error.password.too_short", null, LocaleUtils.getCurrentLocale()));
        }

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
            throw new WeakPasswordException(
                    messageSource.getMessage("error.password.weak", null, LocaleUtils.getCurrentLocale())
            );
        }
    }
}
