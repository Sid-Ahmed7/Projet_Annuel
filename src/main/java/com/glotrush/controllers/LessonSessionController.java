package com.glotrush.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.response.LessonSessionResponse;
import com.glotrush.services.session.ILessonSessionService;
import com.glotrush.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/lesson-sessions")
@RequiredArgsConstructor
public class LessonSessionController {

    private final ILessonSessionService lessonSessionService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<LessonSessionResponse>> getAllSession(Authentication authentication)
    {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        List<LessonSessionResponse> sessions = lessonSessionService.getAllSessionOfAnAccount(accountId);
        return ResponseEntity.ok(sessions);
    }
    
}
