package com.glotrush.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.response.LanguageLevelResponse;
import com.glotrush.dto.response.LastLessonResponse;
import com.glotrush.dto.response.ProgressOverviewResponse;
import com.glotrush.dto.response.UserProgressResponse;
import com.glotrush.services.progress.IProgressService;
import com.glotrush.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/v1/user-progress")
@RequiredArgsConstructor
public class ProgressController {

    private final IProgressService progressService;

    @GetMapping
    public ResponseEntity<ProgressOverviewResponse> getProgressOverview(Authentication authentication,@RequestParam(required = false) String userId) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        ProgressOverviewResponse progress = progressService.getProgressOverview(accountId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<UserProgressResponse> getProgressByTopic(Authentication authentication, @PathVariable UUID topicId) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        UserProgressResponse progress = progressService.getProgressByTopic(accountId, topicId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/language/{languageId}")
    public ResponseEntity<List<UserProgressResponse>> getProgressByLanguage(Authentication authentication, @PathVariable UUID languageId) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        List<UserProgressResponse> progress = progressService.getProgressByLanguage(accountId, languageId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/language-level")
    public ResponseEntity<List<LanguageLevelResponse>> getLanguageLevel(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        List<LanguageLevelResponse> languageLevels = progressService.getLanguageLevel(accountId);
        return ResponseEntity.ok(languageLevels);   
    }

    @GetMapping("/last-lesson")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LastLessonResponse> getLastLesson(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        return progressService.getLastStudiedLesson(accountId).map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build()); 
    }
}