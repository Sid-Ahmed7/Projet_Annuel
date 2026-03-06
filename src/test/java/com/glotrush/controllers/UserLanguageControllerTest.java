package com.glotrush.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glotrush.dto.request.AddUserLanguageRequest;
import com.glotrush.dto.request.UpdateUserLanguageRequest;
import com.glotrush.dto.response.UserLanguageResponse;
import com.glotrush.enumerations.LanguageType;
import com.glotrush.enumerations.ProficiencyLevel;
import com.glotrush.services.languages.IUserLanguageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserLanguageController Integration Tests")
class UserLanguageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IUserLanguageService userLanguageService;

    private final String userId = "550e8400-e29b-41d4-a716-446655440000";

    @Test
    @DisplayName("Should get user languages")
    @WithMockUser(username = userId)
    void shouldGetMyLanguages() throws Exception {
        UserLanguageResponse response = UserLanguageResponse.builder()
                .id(UUID.randomUUID())
                .languageCode("en")
                .languageName("English")
                .languageType(LanguageType.LEARNING)
                .build();

        when(userLanguageService.getUserLanguages(UUID.fromString(userId))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/user-languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].languageCode").value("en"));
    }

    @Test
    @DisplayName("Should get user languages by type")
    @WithMockUser(username = userId)
    void shouldGetMyLanguagesByType() throws Exception {
        UserLanguageResponse response = UserLanguageResponse.builder()
                .id(UUID.randomUUID())
                .languageType(LanguageType.LEARNING)
                .build();

        when(userLanguageService.getUserLanguagesByType(UUID.fromString(userId), LanguageType.LEARNING)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/user-languages/type/LEARNING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].languageType").value("LEARNING"));
    }

    @Test
    @DisplayName("Should add language to user")
    @WithMockUser(username = userId)
    void shouldAddLanguage() throws Exception {
        AddUserLanguageRequest request = AddUserLanguageRequest.builder()
                .languageId(UUID.randomUUID())
                .languageType(LanguageType.LEARNING)
                .proficiencyLevel(ProficiencyLevel.A1)
                .isPrimary(false)
                .build();

        UserLanguageResponse response = UserLanguageResponse.builder()
                .id(UUID.randomUUID())
                .languageType(LanguageType.LEARNING)
                .build();

        when(userLanguageService.addLanguage(eq(UUID.fromString(userId)), any(AddUserLanguageRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/user-languages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.languageType").value("LEARNING"));
    }

    @Test
    @DisplayName("Should update user language")
    @WithMockUser(username = userId)
    void shouldUpdateLanguage() throws Exception {
        UUID languageId = UUID.randomUUID();
        UpdateUserLanguageRequest request = UpdateUserLanguageRequest.builder()
                .languageType(LanguageType.NATIVE)
                .proficiencyLevel(ProficiencyLevel.B2)
                .isPrimary(true)
                .build();

        UserLanguageResponse response = UserLanguageResponse.builder()
                .id(languageId)
                .languageType(LanguageType.NATIVE)
                .build();

        when(userLanguageService.updateLanguage(eq(UUID.fromString(userId)), eq(languageId), any(UpdateUserLanguageRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/user-languages/{languageId}", languageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.languageType").value("NATIVE"));
    }

    @Test
    @DisplayName("Should remove user language")
    @WithMockUser(username = userId)
    void shouldRemoveLanguage() throws Exception {
        UUID languageId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/user-languages/{languageId}", languageId))
                .andExpect(status().isOk());
    }
}
