package com.glotrush.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import com.glotrush.dto.request.AddUserLanguageRequest;
import com.glotrush.dto.request.LoginRequest;
import com.glotrush.dto.request.UpdateUserLanguageRequest;
import com.glotrush.dto.response.UserLanguageResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.enumerations.AccountStatus;
import com.glotrush.enumerations.LanguageType;
import com.glotrush.enumerations.ProficiencyLevel;
import com.glotrush.enumerations.UserRole;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.services.languages.IUserLanguageService;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserLanguageController Integration Tests")
class UserLanguageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private IUserLanguageService userLanguageService;
    
    private static final String TEST_EMAIL = "test@gmail.com";
    private static final String TEST_PASSWORD = "Password123!@#";
    private static final String TEST_USERNAME = "GojoSatoru";

    @BeforeEach
    void setUp() {
        accountsRepository.deleteAll();

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
    @DisplayName("Should get user languages")
    void shouldGetMyLanguages() throws Exception {
        Cookie cookie = logAndGetCookie();
        UserLanguageResponse response = UserLanguageResponse.builder()
                .id(UUID.randomUUID())
                .languageCode("en")
                .languageName("English")
                .languageType(LanguageType.LEARNING)
                .build();

        when(userLanguageService.getUserLanguages(any(UUID.class))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/user-languages")
                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].languageCode").value("en"));
    }

    @Test
    @DisplayName("Should get user languages by type")
    void shouldGetMyLanguagesByType() throws Exception {
        Cookie cookie = logAndGetCookie();
        UserLanguageResponse response = UserLanguageResponse.builder()
                .id(UUID.randomUUID())
                .languageType(LanguageType.LEARNING)
                .build();

        when(userLanguageService.getUserLanguagesByType(any(UUID.class), eq(LanguageType.LEARNING))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/user-languages/type/LEARNING")
                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].languageType").value("LEARNING"));
    }

    @Test
    @DisplayName("Should add language to user")
    void shouldAddLanguage() throws Exception {
        Cookie cookie = logAndGetCookie();
        AddUserLanguageRequest request = AddUserLanguageRequest.builder()
                .languageId(UUID.randomUUID())
                .languageType(LanguageType.LEARNING)
                .proficiencyLevel(ProficiencyLevel.A1)
                .build();

        UserLanguageResponse response = UserLanguageResponse.builder()
                .id(UUID.randomUUID())
                .languageType(LanguageType.LEARNING)
                .build();

        when(userLanguageService.addLanguage(any(UUID.class), any(AddUserLanguageRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/user-languages")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.languageType").value("LEARNING"));
    }

    @Test
    @DisplayName("Should update user language")
    void shouldUpdateLanguage() throws Exception {
        Cookie cookie = logAndGetCookie();
        UUID languageId = UUID.randomUUID();
        UpdateUserLanguageRequest request = UpdateUserLanguageRequest.builder()
                .languageType(LanguageType.NATIVE)
                .proficiencyLevel(ProficiencyLevel.B2)
                .build();

        UserLanguageResponse response = UserLanguageResponse.builder()
                .id(languageId)
                .languageType(LanguageType.NATIVE)
                .build();

        when(userLanguageService.updateLanguage(any(UUID.class), eq(languageId), any(UpdateUserLanguageRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/user-languages/{languageId}", languageId)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.languageType").value("NATIVE"));
    }

    @Test
    @DisplayName("Should remove user language")
    void shouldRemoveLanguage() throws Exception {
        Cookie cookie = logAndGetCookie();
        UUID languageId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/user-languages/{languageId}", languageId)
                .cookie(cookie))
                .andExpect(status().isOk());
    }
}
