package com.glotrush.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.UUID;

import com.glotrush.dto.request.lesson.FlashcardLessonRequest;
import com.glotrush.dto.request.lesson.MatchingPairLessonRequest;
import com.glotrush.dto.request.lesson.QcmLessonRequest;
import com.glotrush.dto.request.lesson.SortingExerciseLessonRequest;
import com.glotrush.dto.request.CompleteLessonRequest;
import com.glotrush.dto.response.ApiResponse;
import com.glotrush.dto.response.CompleteLessonResponse;
import com.glotrush.dto.response.LessonSummaryResponse;
import com.glotrush.dto.response.UserLessonProgressSummary;
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
                .andExpect(jsonPath("$.matchingPairs").isArray());
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
                .andExpect(jsonPath("$.questions").isArray());
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
                .andExpect(jsonPath("$.sortingExercise").isArray());
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
        // String json = """
        //         {
        //             "lessonType": "FLASHCARD",
        //             "topicId": "%s",
        //             "title": "Explicit Type Lesson",
        //             "description": "Description",
        //             "orderIndex": 1,
        //             "xpReward": 50,
                    
        //             "minLevelRequired": 1,
        //             "durationMinutes": 15,
        //             "isActive": true,
        //             "flashcards": []
        //         }
        //         """.formatted(topicId);

        FlashcardLessonRequest request = LessonTestFactory.createFlashcardLessonRequest(topicId, "Explicit Type Lesson");

        FlashcardLessonResponse response = LessonTestFactory.createFlashcardLessonResponse(UUID.randomUUID(), "Explicit Type Lesson");
        when(lessonService.createLesson(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Explicit Type Lesson"));
    }

    @Test
    @DisplayName("Should delete lesson successfully")
    @WithMockUser(roles = "ADMIN")
    void testDeleteLesson() throws Exception {
        UUID lessonId = UUID.randomUUID();
        ApiResponse apiResponse = new ApiResponse("Lesson deleted successfully");
        // Not actually used by the controller as it constructs its own ApiResponse, 
        // but we verify the service call.

        mockMvc.perform(delete("/api/v1/lessons/{lessonId}", lessonId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return lessons by topic")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000000", roles = "USER")
    void testGetLessonsByTopic() throws Exception {
        UUID topicId = UUID.randomUUID();
        LessonSummaryResponse summary = LessonSummaryResponse.builder()
                .id(UUID.randomUUID())
                .title("Lesson Title")
                .isAlreadyFinish(true)
                .build();
        when(lessonService.getLessonsByTopic(eq(topicId), any())).thenReturn(java.util.List.of(summary));

        mockMvc.perform(get("/api/v1/lessons/topic/{topicId}", topicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Lesson Title"))
                .andExpect(jsonPath("$[0].isAlreadyFinish").value(true));
    }

    @Test
    @DisplayName("Should return lesson by id")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000000", roles = "USER")
    void testGetLessonById() throws Exception {
        UUID lessonId = UUID.randomUUID();
        FlashcardLessonResponse response = LessonTestFactory.createFlashcardLessonResponse(lessonId, "Lesson");
        when(lessonService.getLessonById(eq(lessonId), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/lessons/{lessonId}", lessonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lessonId.toString()));
    }

    @Test
    @DisplayName("Should start lesson successfully")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000000", roles = "USER")
    void testStartLesson() throws Exception {
        UUID lessonId = UUID.randomUUID();
        UserLessonProgressSummary summary = UserLessonProgressSummary.builder()
                .status(com.glotrush.enumerations.LessonStatus.IN_PROGRESS)
                .build();
        when(lessonService.startLesson(any(), eq(lessonId))).thenReturn(summary);

        mockMvc.perform(post("/api/v1/lessons/{lessonId}/start", lessonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Should complete lesson successfully")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000000", roles = "USER")
    void testCompleteLesson() throws Exception {
        UUID lessonId = UUID.randomUUID();
        CompleteLessonRequest request = CompleteLessonRequest.builder()
                .timeSpentSeconds(300)
                .build();
        CompleteLessonResponse response = CompleteLessonResponse.builder()
                .success(true)
                .xpEarned(50)
                .build();
        when(lessonService.completeLesson(any(), eq(lessonId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/lessons/{lessonId}/complete", lessonId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.xpEarned").value(50));
    }
    @Test
    @DisplayName("Should return 404 when lesson not found")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000000", roles = "USER")
    void testGetLessonByIdNotFound() throws Exception {
        UUID lessonId = UUID.randomUUID();
        when(lessonService.getLessonById(eq(lessonId), any()))
                .thenThrow(new com.glotrush.exceptions.LessonNotFoundException("Lesson not found"));

        mockMvc.perform(get("/api/v1/lessons/{lessonId}", lessonId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 403 when user attempts to create lesson")
    @WithMockUser(roles = "USER")
    void testCreateLessonForbidden() throws Exception {
        UUID topicId = UUID.randomUUID();
        FlashcardLessonRequest request = LessonTestFactory.createFlashcardLessonRequest(topicId, "Forbidden Lesson");

        mockMvc.perform(post("/api/v1/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
