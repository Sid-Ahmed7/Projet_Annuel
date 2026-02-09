package com.glotrush.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.response.UserProgressResponse;
import com.glotrush.services.progress.IProgressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ProgressController Language-related Integration Tests")
class ProgressControllerLanguageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IProgressService progressService;

    private final String userId = "550e8400-e29b-41d4-a716-446655440000";

    @Test
    @DisplayName("Should get progress by language")
    @WithMockUser(username = userId)
    void shouldGetProgressByLanguage() throws Exception {
        UUID languageId = UUID.randomUUID();
        UserProgressResponse response = UserProgressResponse.builder()
                .id(UUID.randomUUID())
                .languageName("English")
                .level(5)
                .build();

        when(progressService.getProgressByLanguage(UUID.fromString(userId), languageId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/user-progress/language/{languageId}", languageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].languageName").value("English"))
                .andExpect(jsonPath("$[0].level").value(5));
    }
}
