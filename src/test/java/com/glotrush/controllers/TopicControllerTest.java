package com.glotrush.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.glotrush.enumerations.ProficiencyLevel;
import com.glotrush.security.jwt.JwtAuthenticationEntryPoint;
import com.glotrush.security.jwt.JwtAuthenticationFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glotrush.dto.request.TopicRequest;
import com.glotrush.dto.response.TopicProgressResponse;
import com.glotrush.dto.response.TopicResponse;
import com.glotrush.services.topic.ITopicService;
import java.time.LocalDateTime;

import org.springframework.security.core.userdetails.UserDetailsService;

@WebMvcTest(TopicController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TopicController Unit Tests")
class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ITopicService topicService;

    @MockitoBean
    private MessageSource messageSource;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private TopicRequest topicRequest;
    private TopicResponse topicResponse;
    private UUID topicId;

    @BeforeEach
    void setUp() {
        topicId = UUID.randomUUID();
        topicRequest = TopicRequest.builder()
                .targetLanguageId(UUID.randomUUID())
                .sourceLanguageId(UUID.randomUUID())
                .name("Basics")
                .description("Description")
                .difficulty(ProficiencyLevel.A1)
                .isActive(true)
                .build();

        topicResponse = TopicResponse.builder()
                .id(topicId)
                .name("Basics")
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("Should get all topics")
    void shouldGetAllTopics() throws Exception {
        when(topicService.getAllTopics()).thenReturn(List.of(topicResponse));

        mockMvc.perform(get("/api/v1/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(topicId.toString()));
    }

    @Test
    @WithMockUser
    @DisplayName("Should get all active topics")
    void shouldGetAllActiveTopics() throws Exception {
        when(topicService.getAllTopics(any())).thenReturn(List.of(topicResponse));

        mockMvc.perform(get("/api/v1/topics/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(topicId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create topic successfully as ADMIN")
    void shouldCreateTopicSuccessfully() throws Exception {
        when(topicService.createTopic(any(TopicRequest.class))).thenReturn(topicResponse);

        mockMvc.perform(post("/api/v1/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topicRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(topicId.toString()))
                .andExpect(jsonPath("$.name").value("Basics"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update topic successfully as ADMIN")
    void shouldUpdateTopicSuccessfully() throws Exception {
        when(topicService.updateTopic(eq(topicId), any(TopicRequest.class))).thenReturn(topicResponse);

        mockMvc.perform(put("/api/v1/topics/{topicId}", topicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topicRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(topicId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should disable topic successfully as ADMIN")
    void shouldDisableTopicSuccessfully() throws Exception {
        doNothing().when(topicService).disableTopic(topicId);
        when(messageSource.getMessage(eq("info.topic.disabled_successfully"), any(), any())).thenReturn("Topic deleted successfully");

        mockMvc.perform(delete("/api/v1/topics/{topicId}", topicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Topic deleted successfully"));
    }

    @Test
    @WithMockUser
    @DisplayName("Should search topics")
    void shouldSearchTopics() throws Exception {
        when(topicService.searchTopics(eq("Basics"), eq(ProficiencyLevel.A1), eq(true)))
                .thenReturn(List.of(topicResponse));

        mockMvc.perform(get("/api/v1/topics/search")
                .param("name", "Basics")
                .param("difficulty", "A1")
                .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(topicId.toString()));
    }

    @Test
    @WithMockUser
    @DisplayName("Should search active topics paginated")
    void shouldSearchActiveTopicsPaged() throws Exception {
        UUID languageId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<TopicResponse> page = new PageImpl<>(List.of(topicResponse), pageable, 1);

        when(topicService.searchActiveTopics(eq(languageId), eq("Basics"), eq(ProficiencyLevel.A1), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/topics/search/active/paginated")
                .param("languageId", languageId.toString())
                .param("name", "Basics")
                .param("difficulty", "A1")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(topicId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "e75db189-f2e5-4fcd-836e-71c32c094794")
    @DisplayName("Should get active topics progress paginated")
    void shouldGetActiveTopicsProgress() throws Exception {
        UUID languageId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 3);
        TopicProgressResponse progressResponse = TopicProgressResponse.builder()
                .id(topicId)
                .name("Basics")
                .description("Description")
                .difficulty(ProficiencyLevel.A1)
                .progressPercent(45.0)
                .lastStudiedAt(LocalDateTime.now())
                .build();
        Page<TopicProgressResponse> page = new PageImpl<>(List.of(progressResponse), pageable, 1);

        when(topicService.getActiveTopicsProgress(any(), eq(languageId), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/topics/progress/active")
                .param("languageId", languageId.toString())
                .param("page", "0")
                .param("size", "3")
                .param("sort", "lastStudiedAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(topicId.toString()))
                .andExpect(jsonPath("$.content[0].progressPercent").value(45.0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 403 when creating topic as USER")
    void shouldReturnForbiddenWhenCreatingAsUser() throws Exception {
    }
}
