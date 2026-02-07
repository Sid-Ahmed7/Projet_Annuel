package com.glotrush.controllers;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.response.ApiResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.glotrush.dto.request.CompleteLessonRequest;
import com.glotrush.dto.response.CompleteLessonResponse;
import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.UserLessonProgressSummary;
import com.glotrush.services.lesson.ILessonService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class LessonController {
    private final MessageSource messageSource;
    private final ILessonService lessonService;

    protected final Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    @GetMapping("/topic/{topicId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LessonResponse>> getLessonsByTopic(Authentication authentication, @PathVariable UUID topicId) {
        UUID accountId = UUID.fromString(authentication.getName());
        List<LessonResponse> lessons = lessonService.getLessonsByTopic(topicId, accountId);
        return ResponseEntity.ok(lessons);
    }

    @GetMapping("/{lessonId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LessonResponse> getLessonById(Authentication authentication, @PathVariable UUID lessonId) {
        UUID accountId = UUID.fromString(authentication.getName());
        LessonResponse lesson = lessonService.getLessonById(lessonId, accountId);
        return ResponseEntity.ok(lesson);
    }

    @PostMapping("/{lessonId}/start")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserLessonProgressSummary> startLesson(Authentication authentication, @PathVariable UUID lessonId) {
        UUID accountId = UUID.fromString(authentication.getName());
        UserLessonProgressSummary progressSummary = lessonService.startLesson(accountId, lessonId);
        return ResponseEntity.ok(progressSummary);
    }

    @PostMapping("/{lessonId}/complete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CompleteLessonResponse> completeLesson(Authentication authentication, @PathVariable UUID lessonId, @Valid @RequestBody CompleteLessonRequest lessonRequest) {
        UUID accountId = UUID.fromString(authentication.getName());
        CompleteLessonResponse response = lessonService.completeLesson(accountId, lessonId, lessonRequest);
        return ResponseEntity.ok(response);
    }

    /* PARTIE ADMINISTRATEUR */

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LessonResponse> createLesson(@Valid @RequestBody LessonRequest lessonRequest){
        LessonResponse lessonResponse = lessonService.createLesson(lessonRequest);
        return ResponseEntity.ok(lessonResponse);
    }

    @PutMapping("/{lessonId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LessonResponse> updateLesson(@PathVariable UUID lessonId, @Valid @RequestBody LessonRequest lessonRequest){
        LessonResponse lessonResponse = lessonService.updateLesson(lessonId, lessonRequest);
        return ResponseEntity.ok(lessonResponse);
    }

    @DeleteMapping("/{lessonId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteLesson(@PathVariable UUID lessonId){
        lessonService.removeLesson(lessonId);
        return ResponseEntity.ok(new ApiResponse(messageSource.getMessage("info.lesson.deleted_successfully", null, getCurrentLocale())));
    }
}
