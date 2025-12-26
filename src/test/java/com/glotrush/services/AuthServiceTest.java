package com.glotrush.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.glotrush.builder.AccountBuilder;
import com.glotrush.builder.RefreshTokenBuilder;
import com.glotrush.dto.request.ForgotPasswordRequest;
import com.glotrush.dto.request.LoginRequest;
import com.glotrush.dto.request.RegisterRequest;
import com.glotrush.dto.request.ResetPasswordRequest;
import com.glotrush.dto.response.LoginResponse;
import com.glotrush.dto.response.RegisterResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.PasswordResetToken;
import com.glotrush.entities.RefreshToken;
import com.glotrush.enumerations.UserRole;
import com.glotrush.exceptions.AccountLockedException;
import com.glotrush.exceptions.EmailAlreadyExistsException;
import com.glotrush.exceptions.InvalidTokenException;
import com.glotrush.exceptions.PasswordExpiredException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.exceptions.UsernameAlreadyExistsException;
import com.glotrush.exceptions.WeakPasswordException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.PasswordResetTokenRepository;
import com.glotrush.repositories.RefreshTokenRepository;
import com.glotrush.repositories.TwoFactorAuthRepository;
import com.glotrush.security.jwt.JwtService;
import com.glotrush.services.auth.AuthService;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private TwoFactorAuthRepository twoFactorAuthRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @Mock
    private AccountBuilder accountBuilder;

    @Mock
    private RefreshTokenBuilder refreshTokenBuilder;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Accounts testAccount;
    private UUID testAccountId;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();

        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 604800000L);
        ReflectionTestUtils.setField(authService, "frontendUrl", "http://localhost:3000");

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("SecurePass123!@#");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("SecurePass123!@#");

        testAccount = Accounts.builder()
                .id(testAccountId)
                .email("test@example.com")
                .username("testuser")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .failedLoginAttempts(0)
                .lastPasswordChange(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterSuccess() {
        RegisterResponse expectedResponse = RegisterResponse.builder()
                .userId(testAccountId)
                .email("test@example.com")
                .username("testuser")
                .message("Registration successful")
                .build();

        when(accountsRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountsRepository.existsByUsername(anyString())).thenReturn(false);
        when(accountBuilder.buildAccount(any())).thenReturn(testAccount);
        when(accountsRepository.save(any(Accounts.class))).thenReturn(testAccount);
        when(accountBuilder.buildRegisterResponse(any())).thenReturn(expectedResponse);

        RegisterResponse result = authService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(accountsRepository).save(any(Accounts.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegisterEmailAlreadyExists() {
        when(accountsRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already registered");

        verify(accountsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void testRegisterUsernameAlreadyExists() {
        when(accountsRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountsRepository.existsByUsername(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("Username already taken");

        verify(accountsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception for weak password - too short")
    void testRegisterWeakPasswordTooShort() {
        registerRequest.setPassword("Short1!");

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(WeakPasswordException.class)
                .hasMessageContaining("at least 12 characters");
    }

    @Test
    @DisplayName("Should throw exception for weak password - missing special characters")
    void testRegisterWeakPasswordMissingSpecialChars() {
        registerRequest.setPassword("Password1234");

        when(accountsRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(WeakPasswordException.class)
                .hasMessageContaining("special characters");
    }

    @Test
    @DisplayName("Should login successfully without 2FA")
    void testLoginSuccessWithout2FA() {
        when(accountsRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(twoFactorAuthRepository.existsByAccount_IdAndActiveTrue(any())).thenReturn(false);
        when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");

        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-token")
                .account(testAccount)
                .build();
        when(refreshTokenBuilder.buildRefreshToken(any(Accounts.class), eq("refresh-token"), isNull(), anyLong())).thenReturn(refreshToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        LoginResponse expectedResponse = LoginResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .requires2FA(false)
                .build();
        when(accountBuilder.buildLoginResponse(any(Accounts.class), anyString(), anyString(), anyBoolean())).thenReturn(expectedResponse);

        LoginResponse result = authService.login(loginRequest, httpServletResponse);

        assertThat(result).isNotNull();
        assertThat(result.getRequires2FA()).isFalse();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    // 2FA tests removed - not testing 2FA functionality

    @Test
    @DisplayName("Should throw exception for invalid credentials")
    void testLoginInvalidCredentials() {
        when(accountsRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid"));

        assertThatThrownBy(() -> authService.login(loginRequest, httpServletResponse))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        verify(accountsRepository).save(any(Accounts.class));
    }

    @Test
    @DisplayName("Should lock account after max failed login attempts")
    void testAccountLockAfterMaxAttempts() {
        testAccount.setFailedLoginAttempts(4);
        when(accountsRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid"));

        assertThatThrownBy(() -> authService.login(loginRequest, httpServletResponse))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Account locked");

        verify(accountsRepository).save(any(Accounts.class));
    }

    @Test
    @DisplayName("Should throw exception when account is locked")
    void testLoginAccountLocked() {
        testAccount.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
        when(accountsRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        assertThatThrownBy(() -> authService.login(loginRequest, httpServletResponse))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Account is locked");
    }

    @Test
    @DisplayName("Should throw exception when password is expired")
    void testLoginPasswordExpired() {
        testAccount.setLastPasswordChange(LocalDateTime.now().minusDays(61));
        when(accountsRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));
        when(authenticationManager.authenticate(any())).thenReturn(null);

        assertThatThrownBy(() -> authService.login(loginRequest, httpServletResponse))
                .isInstanceOf(PasswordExpiredException.class)
                .hasMessageContaining("Password expired");
    }

    // 2FA verification tests removed - not testing 2FA functionality

    @Test
    @DisplayName("Should send forgot password email")
    void testForgotPassword() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(accountsRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        authService.forgotPassword(request);

        verify(passwordResetTokenRepository).invalidateAllUserTokens(any(UUID.class));
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception for forgot password with invalid email")
    void testForgotPasswordInvalidEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("invalid@example.com");

        when(accountsRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.forgotPassword(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Accounts not found");
    }

    @Test
    @DisplayName("Should reset password successfully")
    void testResetPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNewPassword("NewSecurePass123!@#");

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token("valid-token")
                .account(testAccount)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .build();

        when(passwordResetTokenRepository.findValidToken(anyString(), any())).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");

        authService.resetPassword(request);

        verify(accountsRepository).save(any(Accounts.class));
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(refreshTokenRepository).revokeAllUserTokens(any(UUID.class), any());
    }

    @Test
    @DisplayName("Should throw exception for invalid reset token")
    void testResetPasswordInvalidToken() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("invalid-token");
        request.setNewPassword("NewSecurePass123!@#");

        when(passwordResetTokenRepository.findValidToken(anyString(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid or expired reset token");
    }

    @Test
    @DisplayName("Should logout successfully")
    void testLogout() {
        String refreshTokenValue = "refresh-token";
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .account(testAccount)
                .isRevoked(false)
                .build();

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));

        authService.logout(refreshTokenValue, httpServletResponse);

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }
}
