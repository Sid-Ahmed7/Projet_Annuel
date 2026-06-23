package com.glotrush.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import com.glotrush.dto.response.UserProgressResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.enumerations.AccountStatus;
import com.glotrush.enumerations.UserRole;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.services.progress.IProgressService;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ProgressController Language-related Integration Tests")
class ProgressControllerLanguageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private IProgressService progressService;

    private static final String TEST_EMAIL = "test@gmail.com";
    private static final String TEST_PASSWORD = "Password123!@#";
    private static final String TEST_USERNAME = "GojoSatoru";

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE accounts CASCADE");

        accountsRepository.save(Accounts.builder()
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .username(TEST_USERNAME)
                .firstName("Gojo").lastName("Satoru")
                .role(UserRole.USER).status(AccountStatus.ACTIVE)
                .failedLoginAttempts(0)
                .lastPasswordChange(LocalDateTime.now())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build());
    }

    private Cookie logAndGetCookie() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getCookie("access_token");
    }

    @Test
    @DisplayName("Should get progress by language")
    void shouldGetProgressByLanguage() throws Exception {
        Cookie cookie = logAndGetCookie();
        UUID languageId = UUID.randomUUID();
        UserProgressResponse response = UserProgressResponse.builder()
                .id(UUID.randomUUID())
                .languageName("English")
                .level(5)
                .build();

        when(progressService.getProgressByLanguage(any(UUID.class), any(UUID.class))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/user-progress/language/{languageId}", languageId)
                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].languageName").value("English"))
                .andExpect(jsonPath("$[0].level").value(5));
    }
}
