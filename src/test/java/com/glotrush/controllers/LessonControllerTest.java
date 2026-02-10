package com.glotrush.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.UUID;

import com.glotrush.config.TestMessageSourceConfig;
import com.glotrush.dto.request.lesson.FlashcardLessonRequest;
import com.glotrush.dto.request.lesson.MatchingPairLessonRequest;
import com.glotrush.dto.request.lesson.QcmLessonRequest;
import com.glotrush.dto.request.lesson.SortingExerciseLessonRequest;
import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.lesson.FlashcardLessonResponse;
import com.glotrush.dto.response.lesson.MatchingPairLessonResponse;
import com.glotrush.dto.response.lesson.QcmLessonResponse;
import com.glotrush.dto.response.lesson.SortingExerciseLessonResponse;
import com.glotrush.factory.LessonTestFactory;
import com.glotrush.services.lesson.ILessonService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ILessonService lessonService;

    @Test
    @DisplayName("Should create lesson successfully and return FlashcardLessonResponse")
    @WithMockUser(roles = "ADMIN")
    void testCreateLesson() throws Exception {
        UUID topicId = UUID.randomUUID();
        FlashcardLessonRequest request = LessonTestFactory.createFlashcardLessonRequest(topicId, "New Lesson");
        FlashcardLessonResponse response = LessonTestFactory.createFlashcardLessonResponse(UUID.randomUUID(), "New Lesson");

        when(lessonService.createLesson(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Lesson"))
                .andExpect(jsonPath("$.flashcards").isArray());
    }

    @Test
    @DisplayName("Should create MatchingPairLesson successfully and return MatchingPairLessonResponse")
    @WithMockUser(roles = "ADMIN")
    void testCreateMatchingPairLesson() throws Exception {
        UUID topicId = UUID.randomUUID();
        MatchingPairLessonRequest request = LessonTestFactory.createMatchingPairLessonRequest(topicId, "Matching Pair Lesson");
        MatchingPairLessonResponse response = LessonTestFactory.createMatchingPairLessonResponse(UUID.randomUUID(), "Matching Pair Lesson");

        when(lessonService.createLesson(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Matching Pair Lesson"))
                .andExpect(jsonPath("$.matchingPairResponses").isArray());
    }

    @Test
    @DisplayName("Should create QcmLesson successfully and return QcmLessonResponse")
    @WithMockUser(roles = "ADMIN")
    void testCreateQcmLesson() throws Exception {
        UUID topicId = UUID.randomUUID();
        QcmLessonRequest request = LessonTestFactory.createQcmLessonRequest(topicId, "QCM Lesson");
        QcmLessonResponse response = LessonTestFactory.createQcmLessonResponse(UUID.randomUUID(), "QCM Lesson");

        when(lessonService.createLesson(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("QCM Lesson"))
                .andExpect(jsonPath("$.qcmQuestionResponses").isArray());
    }

    @Test
    @DisplayName("Should create SortingExerciseLesson successfully and return SortingExerciseLessonResponse")
    @WithMockUser(roles = "ADMIN")
    void testCreateSortingExerciseLesson() throws Exception {
        UUID topicId = UUID.randomUUID();
        SortingExerciseLessonRequest request = LessonTestFactory.createSortingExerciseLessonRequest(topicId, "Sorting Exercise Lesson");
        SortingExerciseLessonResponse response = LessonTestFactory.createSortingExerciseLessonResponse(UUID.randomUUID(), "Sorting Exercise Lesson");

        when(lessonService.createLesson(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sorting Exercise Lesson"))
                .andExpect(jsonPath("$.sortingExerciseResponses").isArray());
    }

    @Test
    @DisplayName("Should update lesson successfully and return FlashcardLessonResponse")
    @WithMockUser(roles = "ADMIN")
    void testUpdateLesson() throws Exception {
        UUID lessonId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();
        FlashcardLessonRequest request = LessonTestFactory.createFlashcardLessonRequest(topicId, "Updated Lesson");
        FlashcardLessonResponse response = LessonTestFactory.createFlashcardLessonResponse(lessonId, "Updated Lesson");

        when(lessonService.updateLesson(eq(lessonId), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/lessons/{lessonId}", lessonId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lessonId.toString()))
                .andExpect(jsonPath("$.title").value("Updated Lesson"))
                .andExpect(jsonPath("$.flashcards").isArray());
    }

    @Test
    @DisplayName("Should fail when lessonType is missing")
    @WithMockUser(roles = "ADMIN")
    void testCreateLessonMissingType() throws Exception {
        String json = """
                {
                    "topicId": "%s",
                    "title": "Missing Type Lesson",
                    "description": "Description",
                    "orderIndex": 1,
                    "xpReward": 50,
                    "isLocked": false,
                    "minLevelRequired": 1,
                    "durationMinutes": 15,
                    "isActive": true
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should create lesson successfully when lessonType is provided in JSON")
    @WithMockUser(roles = "ADMIN")
    void testCreateLessonWithExplicitType() throws Exception {
        UUID topicId = UUID.randomUUID();
        String json = """
                {
                    "lessonType": "FLASHCARD",
                    "topicId": "%s",
                    "title": "Explicit Type Lesson",
                    "description": "Description",
                    "orderIndex": 1,
                    "xpReward": 50,
                    "isLocked": false,
                    "minLevelRequired": 1,
                    "durationMinutes": 15,
                    "isActive": true,
                    "flashcards": []
                }
                """.formatted(topicId);

        FlashcardLessonResponse response = LessonTestFactory.createFlashcardLessonResponse(UUID.randomUUID(), "Explicit Type Lesson");
        when(lessonService.createLesson(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Explicit Type Lesson"));
    }

    @Test
    @DisplayName("Should delete lesson successfully")
    @WithMockUser(roles = "ADMIN")
    void testDeleteLesson() throws Exception {
        UUID lessonId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/lessons/{lessonId}", lessonId))
                .andExpect(status().isOk());
    }
}
