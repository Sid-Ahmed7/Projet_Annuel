package com.glotrush.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glotrush.dto.request.LanguageRequest;
import com.glotrush.dto.request.LoginRequest;
import com.glotrush.dto.response.LanguageResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.enumerations.AccountStatus;
import com.glotrush.enumerations.UserRole;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.LanguageRepository;
import com.glotrush.security.jwt.JwtService;
import com.glotrush.services.languages.ILanguageService;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("LanguageController Integration Tests")
class LanguageControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ILanguageService languageService;

    private static final String TEST_EMAIL = "test@gmail.com";
    private static final String TEST_PASSWORD = "Password123!@#";
    private static final String TEST_USERNAME = "GojoSatoru";

    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String ADMIN_PASSWORD = "Admin123!@#";

    @BeforeEach
    void setUp() {
        languageRepository.deleteAll();
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

        accountsRepository.save(Accounts.builder()
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .username("AdminUser")
                .firstName("Admin").lastName("Admin")
                .role(UserRole.ADMIN).status(AccountStatus.ACTIVE)
                .failedLoginAttempts(0)
                .lastPasswordChange(LocalDateTime.now())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build());
    }

    private Cookie getAdminCookie() {
        Accounts admin = accountsRepository.findByEmail(ADMIN_EMAIL).orElseThrow();
        String token = jwtService.generateAccessToken(admin.getId().toString(), "ADMIN");
        return new Cookie("access_token", token);
    }

    private Cookie logAndGetCookie(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getCookie("access_token");
    }

    @Test
    @DisplayName("Should get all languages")
    void shouldGetAllLanguages() throws Exception {
        Cookie cookie = logAndGetCookie(TEST_EMAIL, TEST_PASSWORD);
        LanguageResponse response = LanguageResponse.builder()
                .id(UUID.randomUUID())
                .code("en")
                .name("English")
                .isActive(true)
                .build();

        when(languageService.getAllLanguages()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/languages")
                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("en"))
                .andExpect(jsonPath("$[0].name").value("English"));
    }

    @Test
    @DisplayName("Should get all active languages")
    void shouldGetAllActiveLanguages() throws Exception {
        Cookie cookie = logAndGetCookie(TEST_EMAIL, TEST_PASSWORD);
        LanguageResponse response = LanguageResponse.builder()
                .id(UUID.randomUUID())
                .code("en")
                .name("English")
                .isActive(true)
                .build();

        when(languageService.getAllActiveLanguages()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/languages/active")
                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("en"))
                .andExpect(jsonPath("$[0].name").value("English"));
    }

    @Test
    @DisplayName("Should get language by ID")
    void shouldGetLanguageById() throws Exception {
        Cookie cookie = logAndGetCookie(TEST_EMAIL, TEST_PASSWORD);
        UUID id = UUID.randomUUID();
        LanguageResponse response = LanguageResponse.builder()
                .id(id)
                .code("fr")
                .name("French")
                .build();

        when(languageService.getLanguageById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/languages/{id}", id)
                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("fr"));
    }

    @Test
    @DisplayName("Should get language by code")
    void shouldGetLanguageByCode() throws Exception {
        Cookie cookie = logAndGetCookie(TEST_EMAIL, TEST_PASSWORD);
        LanguageResponse response = LanguageResponse.builder()
                .id(UUID.randomUUID())
                .code("es")
                .name("Spanish")
                .build();

        when(languageService.getLanguageByCode("es")).thenReturn(response);

        mockMvc.perform(get("/api/v1/languages/code/es")
                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("es"));
    }

    @Test
    @DisplayName("Should create language when admin")
    void shouldCreateLanguage() throws Exception {
        Cookie cookie = getAdminCookie();
        LanguageRequest request = new LanguageRequest();
        request.setCode("it");
        request.setName("Italian");
        request.setIsActive(true);

        LanguageResponse response = LanguageResponse.builder()
                .id(UUID.randomUUID())
                .code("it")
                .name("Italian")
                .build();

        when(languageService.createLanguage(any(LanguageRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/languages")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("it"));
    }

    @Test
    @DisplayName("Should update language when admin")
    void shouldUpdateLanguage() throws Exception {
        Cookie cookie = getAdminCookie();
        UUID id = UUID.randomUUID();
        LanguageRequest request = new LanguageRequest();
        request.setCode("pt");
        request.setName("Portuguese");
        request.setIsActive(true);

        LanguageResponse response = LanguageResponse.builder()
                .id(id)
                .code("pt")
                .name("Portuguese")
                .build();

        when(languageService.updateLanguage(eq(id), any(LanguageRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/languages/{languageId}", id)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("pt"));
    }

    @Test
    @DisplayName("Should delete language when admin")
    void shouldDeleteLanguage() throws Exception {
        Cookie cookie = getAdminCookie();
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/languages/{languageId}", id)
                .cookie(cookie))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 403 when creating language as non-admin")
    void shouldReturn403WhenCreatingAsUser() throws Exception {
        Cookie cookie = logAndGetCookie(TEST_EMAIL, TEST_PASSWORD);
        LanguageRequest request = new LanguageRequest();
        request.setCode("it");
        request.setName("Italian");

        mockMvc.perform(post("/api/v1/languages")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
