package com.glotrush.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glotrush.dto.request.ForgotPasswordRequest;
import com.glotrush.dto.request.LoginRequest;
import com.glotrush.dto.request.RegisterRequest;
import com.glotrush.dto.request.ResetPasswordRequest;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.PasswordResetToken;
import com.glotrush.enumerations.AccountStatus;
import com.glotrush.enumerations.UserRole;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.PasswordResetTokenRepository;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Accounts testAccount;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "SecurePass123!@#";
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        accountsRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();

        testAccount = Accounts.builder()
                .email(TEST_EMAIL)
                .username(TEST_USERNAME)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .status(AccountStatus.ACTIVE)
                .failedLoginAttempts(0)
                .lastPasswordChange(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setUsername("newuser");
        request.setPassword("NewSecurePass123!@#");
        request.setFirstName("New");
        request.setLastName("User");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")  // Force la locale anglaise
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @DisplayName("Should return 400 when registering with existing email")
    void testRegisterWithExistingEmail() throws Exception {
        accountsRepository.save(testAccount);

        RegisterRequest request = new RegisterRequest();
        request.setEmail(TEST_EMAIL);
        request.setUsername("differentuser");
        request.setPassword("SecurePass123!@#");
        request.setFirstName("Test");
        request.setLastName("User");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return 400 when registering with weak password")
    void testRegisterWithWeakPassword() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setUsername("newuser");
        request.setPassword("weak");
        request.setFirstName("New");
        request.setLastName("User");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() throws Exception {
        accountsRepository.save(testAccount);

        LoginRequest request = new LoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("Should return 401 with invalid credentials")
    void testLoginWithInvalidCredentials() throws Exception {
        accountsRepository.save(testAccount);

        LoginRequest request = new LoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword("WrongPassword123!@#");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 with non-existent email")
    void testLoginWithNonExistentEmail() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should send forgot password email successfully")
    void testForgotPassword() throws Exception {
        accountsRepository.save(testAccount);

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(TEST_EMAIL);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset email sent"));
    }

    @Test
    @DisplayName("Should reset password successfully with valid token")
    void testResetPassword() throws Exception {
        Accounts savedAccount = accountsRepository.save(testAccount);

        String resetToken = UUID.randomUUID().toString();
        PasswordResetToken token = PasswordResetToken.builder()
                .token(resetToken)
                .account(savedAccount)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .build();
        passwordResetTokenRepository.save(token);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(resetToken);
        request.setNewPassword("NewSecurePass456!@#");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }

    @Test
    @DisplayName("Should return 400 with invalid reset token")
    void testResetPasswordWithInvalidToken() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("invalid-token");
        request.setNewPassword("NewSecurePass456!@#");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 with expired reset token")
    void testResetPasswordWithExpiredToken() throws Exception {
        Accounts savedAccount = accountsRepository.save(testAccount);

        String resetToken = UUID.randomUUID().toString();
        PasswordResetToken token = PasswordResetToken.builder()
                .token(resetToken)
                .account(savedAccount)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .isUsed(false)
                .build();
        passwordResetTokenRepository.save(token);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(resetToken);
        request.setNewPassword("NewSecurePass456!@#");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should get current user info when authenticated")
    void testGetCurrentUser() throws Exception {
        accountsRepository.save(testAccount);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        Cookie cookie = loginResult.getResponse().getCookie("access_token");

        mockMvc.perform(get("/api/v1/auth/me")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.username").value(TEST_USERNAME));
    }

    @Test
    @DisplayName("Should return 401 when accessing protected endpoint without authentication")
    void testGetCurrentUserWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should validate email format on registration")
    void testRegisterWithInvalidEmailFormat() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email");
        request.setUsername("newuser");
        request.setPassword("SecurePass123!@#");
        request.setFirstName("New");
        request.setLastName("User");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate required fields on registration")
    void testRegisterWithMissingFields() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should lock account after multiple failed login attempts")
    void testAccountLockAfterFailedAttempts() throws Exception {
        accountsRepository.save(testAccount);

        LoginRequest request = new LoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword("WrongPassword123!@#");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isLocked());
    }
}
