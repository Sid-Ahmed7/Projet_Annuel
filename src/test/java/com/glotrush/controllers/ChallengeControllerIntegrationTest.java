package com.glotrush.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glotrush.dto.request.LoginRequest;
import com.glotrush.dto.request.challenge.ChallengeResponseRequest;
import com.glotrush.dto.request.challenge.CreateChallengeRequest;
import com.glotrush.dto.response.challenge.ChallengeResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.enumerations.AccountStatus;
import com.glotrush.enumerations.ChallengeType;
import com.glotrush.enumerations.UserRole;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.services.challenge.IChallengeService;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ChallengeController Integration Tests")
public class ChallengeControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private IChallengeService challengeService;

    private static final String TEST_EMAIL = "test@gmail.com";
    private static final String TEST_PASSWORD = "Password123!@#";
    private static final String TEST_USERNAME = "GojoSatoru";

    private UUID userId;
    private UUID challengeId;
    private ChallengeResponse challengeResponse;
    private Accounts account;

    @BeforeEach
    void setUp() throws Exception {
        accountsRepository.deleteAll();

        account = Accounts.builder()
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .username(TEST_USERNAME)
                .firstName("Gojo")
                .lastName("Satoru")
                .role(UserRole.USER)
                .status(AccountStatus.ACTIVE)
                .failedLoginAttempts(0)
                .lastPasswordChange(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        accountsRepository.save(account);
        userId = account.getId();
        challengeId = UUID.randomUUID();
        challengeResponse = ChallengeResponse.builder().build();
    }

    private Cookie logAndGetCookie() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getCookie("access_token");
    }

    @Test
    @DisplayName("Should create challenge")
    void createChallenge_ok() throws Exception {
        Cookie cookie = logAndGetCookie();
        CreateChallengeRequest request = new CreateChallengeRequest();
        request.setTitle("Test Challenge");
        request.setChallengeType(ChallengeType.PUBLIC);
        request.setLanguageId(UUID.randomUUID());

        when(challengeService.createChallenge(any(UUID.class), any())).thenReturn(challengeResponse);

        mockMvc.perform(post("/api/v1/challenges")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return unauthorized when creating challenge without authentication")
    void createChallenge_unauthorized() throws Exception {
        CreateChallengeRequest request = new CreateChallengeRequest();
        request.setChallengeType(ChallengeType.PUBLIC);
        request.setLanguageId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/challenges")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return challenge details")
    void getChallenge_ok() throws Exception {
        Cookie cookie = logAndGetCookie();
        when(challengeService.getChallenge(any(UUID.class), any(UUID.class))).thenReturn(challengeResponse);
        mockMvc.perform(get("/api/v1/challenges/{id}", challengeId)
                .cookie(cookie))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return unauthorized when getting challenge without authentication")
    void getChallenge_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/challenges/{id}", challengeId)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return user challenges")
    void getUserChallenges_ok() throws Exception {
        Cookie cookie = logAndGetCookie();
        when(challengeService.getUserChallenge(any(UUID.class))).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/challenges/my-challenges")
                .cookie(cookie))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return all public challenges")
    void getAllPublicChallenges_ok() throws Exception {
        Cookie cookie = logAndGetCookie();
        when(challengeService.getPublicChallenges()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/challenges/all-public-challenges")
                .cookie(cookie))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should join challenge ")
    void joinChallenge_ok() throws Exception {
        Cookie cookie = logAndGetCookie();
        when(challengeService.joinChallenge(any(UUID.class), any(UUID.class))).thenReturn(challengeResponse);
        mockMvc.perform(post("/api/v1/challenges/{id}/join", challengeId)
                .cookie(cookie))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return unauthorized when joining challenge without authentication")
    void joinChallenge_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/challenges/{id}/join", challengeId)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should accept challenge ")
    void acceptChallenge_ok() throws Exception {
        Cookie cookie = logAndGetCookie();
        when(challengeService.acceptChallenge(any(UUID.class), any(UUID.class))).thenReturn(challengeResponse);
        mockMvc.perform(put("/api/v1/challenges/{id}/accept", challengeId)
                .cookie(cookie))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should decline challenge ")
    void declineChallenge_ok() throws Exception {
        Cookie cookie = logAndGetCookie();
        when(challengeService.declineChallenge(any(UUID.class), any(UUID.class))).thenReturn(challengeResponse);
        mockMvc.perform(put("/api/v1/challenges/{id}/decline", challengeId)
                .cookie(cookie))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should cancel challenge ")
    void cancelChallenge_ok() throws Exception {
        Cookie cookie = logAndGetCookie();
        when(challengeService.cancelChallenge(any(UUID.class), any(UUID.class))).thenReturn(challengeResponse);
        mockMvc.perform(put("/api/v1/challenges/{id}/cancel", challengeId)
                .cookie(cookie))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should submit challenge result ")
    void submitChallengeResult_ok() throws Exception {
        Cookie cookie = logAndGetCookie();
        ChallengeResponseRequest request = new ChallengeResponseRequest();
        request.setScore(80.0);
        request.setTimePassed(120L);
        when(challengeService.submitChallenge(any(UUID.class), any(UUID.class), any())).thenReturn(challengeResponse);
        mockMvc.perform(post("/api/v1/challenges/{id}/submit-result", challengeId)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return unauthorized when submitting challenge result without authentication")
    void submitChallengeResult_unauthorized() throws Exception {
        ChallengeResponseRequest request = new ChallengeResponseRequest();
        request.setScore(80.0);
        request.setTimePassed(120L);
        mockMvc.perform(post("/api/v1/challenges/{id}/submit-result", challengeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}

