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
import com.glotrush.dto.request.LanguageRequest;
import com.glotrush.dto.response.LanguageResponse;
import com.glotrush.services.languages.ILanguageService;
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
@DisplayName("LanguageController Integration Tests")
class LanguageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ILanguageService languageService;

    @Test
    @DisplayName("Should get all active languages")
    @WithMockUser
    void shouldGetAllLanguages() throws Exception {
        LanguageResponse response = LanguageResponse.builder()
                .id(UUID.randomUUID())
                .code("en")
                .name("English")
                .isActive(true)
                .build();

        when(languageService.getAllActiveLanguages()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("en"))
                .andExpect(jsonPath("$[0].name").value("English"));
    }

    @Test
    @DisplayName("Should get language by ID")
    @WithMockUser
    void shouldGetLanguageById() throws Exception {
        UUID id = UUID.randomUUID();
        LanguageResponse response = LanguageResponse.builder()
                .id(id)
                .code("fr")
                .name("French")
                .build();

        when(languageService.getLanguageById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/languages/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("fr"));
    }

    @Test
    @DisplayName("Should get language by code")
    @WithMockUser
    void shouldGetLanguageByCode() throws Exception {
        LanguageResponse response = LanguageResponse.builder()
                .id(UUID.randomUUID())
                .code("es")
                .name("Spanish")
                .build();

        when(languageService.getLanguageByCode("es")).thenReturn(response);

        mockMvc.perform(get("/api/v1/languages/code/es"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("es"));
    }

    @Test
    @DisplayName("Should create language when admin")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateLanguage() throws Exception {
        LanguageRequest request = new LanguageRequest();
        request.setCode("it");
        request.setName("Italian");
        request.setOrderIndex(0);
        request.setIsActive(true);

        LanguageResponse response = LanguageResponse.builder()
                .id(UUID.randomUUID())
                .code("it")
                .name("Italian")
                .build();

        when(languageService.createLanguage(any(LanguageRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/languages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("it"));
    }

    @Test
    @DisplayName("Should update language when admin")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateLanguage() throws Exception {
        UUID id = UUID.randomUUID();
        LanguageRequest request = new LanguageRequest();
        request.setCode("pt");
        request.setName("Portuguese");
        request.setOrderIndex(0);
        request.setIsActive(true);

        LanguageResponse response = LanguageResponse.builder()
                .id(id)
                .code("pt")
                .name("Portuguese")
                .build();

        when(languageService.updateLanguage(eq(id), any(LanguageRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/languages/{languageId}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("pt"));
    }

    @Test
    @DisplayName("Should delete language when admin")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteLanguage() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/languages/{languageId}", id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 403 when creating language as non-admin")
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenCreatingAsUser() throws Exception {
        LanguageRequest request = new LanguageRequest();
        request.setCode("it");
        request.setName("Italian");

        mockMvc.perform(post("/api/v1/languages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
