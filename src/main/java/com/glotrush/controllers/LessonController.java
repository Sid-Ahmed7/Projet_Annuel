package com.glotrush.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.request.CompleteLessonRequest;
import com.glotrush.dto.response.CompleteLessonResponse;
import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.UserLessonProgressSummary;
import com.glotrush.services.lesson.ILessonService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class LessonController {
    
    private final ILessonService lessonService;

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<LessonResponse>> getLessonsByTopic(Authentication authentication, @PathVariable UUID topicId) {
        UUID accountId = UUID.fromString(authentication.getName());
        List<LessonResponse> lessons = lessonService.getLessonsByTopic(topicId, accountId);
        return ResponseEntity.ok(lessons);
    }


    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonResponse> getLessonById(Authentication authentication, @PathVariable UUID lessonId) {
        UUID accountId = UUID.fromString(authentication.getName());
        LessonResponse lesson = lessonService.getLessonById(lessonId, accountId);
        return ResponseEntity.ok(lesson);
    }

    @PostMapping("/{lessonId}/start")
    public ResponseEntity<UserLessonProgressSummary> startLesson(Authentication authentication, @PathVariable UUID lessonId) {
        UUID accountId = UUID.fromString(authentication.getName());
        UserLessonProgressSummary progressSummary = lessonService.startLesson(accountId, lessonId);
        return ResponseEntity.ok(progressSummary);
    }

    @PostMapping("/{lessonId}/complete")
    public ResponseEntity<CompleteLessonResponse> completeLesson(Authentication authentication, @PathVariable UUID lessonId, @Valid @RequestBody CompleteLessonRequest lessonRequest) {
        UUID accountId = UUID.fromString(authentication.getName());
        CompleteLessonResponse response = lessonService.completeLesson(accountId, lessonId, lessonRequest);
        return ResponseEntity.ok(response);
    }   
    
}
