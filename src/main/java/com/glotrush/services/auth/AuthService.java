package com.glotrush.services.auth;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.glotrush.builder.AccountBuilder;
import com.glotrush.builder.RefreshTokenBuilder;
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
import com.glotrush.exceptions.AccountLockedException;
import com.glotrush.exceptions.EmailAlreadyExistsException;
import com.glotrush.exceptions.InvalidTokenException;
import com.glotrush.exceptions.PasswordExpiredException;
import com.glotrush.exceptions.TwoFactorNotEnabledException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.exceptions.UsernameAlreadyExistsException;
import com.glotrush.exceptions.WeakPasswordException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.PasswordResetTokenRepository;
import com.glotrush.repositories.RefreshTokenRepository;
import com.glotrush.repositories.TwoFactorAuthRepository;
import com.glotrush.security.jwt.JwtService;
import com.glotrush.services.EmailService;

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


    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int ACCOUNT_LOCK_DURATION_MINUTES = 30;
    private static final int PASSWORD_EXPIRY_DAYS = 60;

    protected final Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (accountsRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(messageSource.getMessage("error.auth.email_already_registered", null, getCurrentLocale()));
        }

        if (request.getUsername() != null && accountsRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(messageSource.getMessage("error.auth.username_already_taken", null, getCurrentLocale()));
        }

        validatePasswordStrength(request.getPassword());

        Accounts account = accountBuilder.buildAccount(request);

        Accounts savedAccount = accountsRepository.save(account);
        log.info("User registered successfully: {}", savedAccount.getEmail());

        return accountBuilder.buildRegisterResponse(savedAccount);
    }

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        Accounts account = accountsRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException(messageSource.getMessage("error.auth.invalid_credentials", null, getCurrentLocale())));

        checkAccountLock(account);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            resetFailedLoginAttempts(account);
            checkPasswordExpiry(account);

            boolean requires2FA = twoFactorAuthRepository.existsByAccount_IdAndActiveTrue(account.getId());

            if (requires2FA) {
                return LoginResponse.builder()
                        .requires2FA(true)
                        .tempUserId(account.getId().toString())
                        .message(messageSource.getMessage("success.2fa.code_required", null, getCurrentLocale()))
                        .build();
            }

            return generateAuthTokens(account, response);

        } catch (BadCredentialsException e) {
            handleFailedLogin(account);
            throw new BadCredentialsException(messageSource.getMessage("error.auth.invalid_credentials", null, getCurrentLocale()));
        }
    }

    @Transactional
    public LoginResponse verify2FA(Verify2FARequest request, HttpServletResponse response) {
        UUID userId = UUID.fromString(request.getTempUserId());
        Accounts account = accountsRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, getCurrentLocale())));

        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findFirstByAccount_IdAndActiveTrue(userId)
                .orElseThrow(() -> new TwoFactorNotEnabledException(messageSource.getMessage("error.2fa.not_enabled", null, getCurrentLocale())));

        twoFactorAuth.setLastUsedAt(LocalDateTime.now());
        twoFactorAuthRepository.save(twoFactorAuth);

        return generateAuthTokens(account, response);
    }

    @Transactional
    public RefreshTokenResponse refreshToken(String refreshToken, HttpServletResponse response) {
        RefreshToken token = refreshTokenRepository.findValidToken(refreshToken, LocalDateTime.now())
                .orElseThrow(() -> new InvalidTokenException(messageSource.getMessage("error.auth.invalid_token", null, getCurrentLocale())));

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
                .message(messageSource.getMessage("success.auth.tokens_refreshed", null, getCurrentLocale()))
                .build();
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Accounts account = accountsRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, getCurrentLocale())));

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

        log.info("Password reset email sent to: {}", account.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findValidToken(request.getToken(), LocalDateTime.now()).orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        Accounts account = resetToken.getAccount();

        validatePasswordStrength(request.getNewPassword());

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        account.setLastPasswordChange(LocalDateTime.now());
        accountsRepository.save(account);

        resetToken.setIsUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.revokeAllUserTokens(account.getId(), LocalDateTime.now());

        log.info("Password reset successfully for account: {}", account.getEmail());
    }

    @Transactional
    public void logout(String refreshToken, HttpServletResponse response) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setIsRevoked(true);
                    refreshTokenRepository.save(token);
                });

        clearAuthCookies(response);
        log.info("User logged out successfully");
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
            throw new AccountLockedException(messageSource.getMessage("error.auth.account_locked_until", null, getCurrentLocale()) + account.getAccountLockedUntil());
        }
    }

    private void handleFailedLogin(Accounts account) {
        int attempts = account.getFailedLoginAttempts() + 1;
        
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(ACCOUNT_LOCK_DURATION_MINUTES);
            account.setAccountLockedUntil(lockUntil);
            account.setFailedLoginAttempts(attempts);
            accountsRepository.save(account);
            throw new AccountLockedException(messageSource.getMessage("error.auth.account_locked_attempts", null, getCurrentLocale()));
        } else {
            account.setFailedLoginAttempts(attempts);
            accountsRepository.save(account);
        }
    }

    private void resetFailedLoginAttempts(Accounts account) {
        if (account.getFailedLoginAttempts() > 0) {
            account.setFailedLoginAttempts(0);
            account.setAccountLockedUntil(null);
            accountsRepository.save(account);
        }
    }

    private void checkPasswordExpiry(Accounts account) {
        LocalDateTime expiryDate = account.getLastPasswordChange().plusDays(PASSWORD_EXPIRY_DAYS);
        if (LocalDateTime.now().isAfter(expiryDate)) {
            throw new PasswordExpiredException(messageSource.getMessage("error.auth.password_expired", null, getCurrentLocale()));
        }
    }

    private void validatePasswordStrength(String password) {
        if (password.length() < 12) {
            throw new WeakPasswordException(messageSource.getMessage("error.password.too_short", null, getCurrentLocale()));
        }

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
            throw new WeakPasswordException(
                    messageSource.getMessage("error.password.weak", null, getCurrentLocale())
            );
        }
    }
}
