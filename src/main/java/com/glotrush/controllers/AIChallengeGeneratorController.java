package com.glotrush.controllers;

import java.util.UUID;

import com.glotrush.builder.ChallengeBuilder;
import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.request.ai.AIChallengeGenerateRequest;
import com.glotrush.dto.response.ai.AIChallengeContentResponse;
import com.glotrush.services.ai.IAILessonGeneratorService;
import com.glotrush.utils.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
public class AIChallengeGeneratorController {

    private final IAILessonGeneratorService aiLessonGeneratorService;
    private final ChallengeBuilder challengeBuilder;

    @PostMapping("/generate-content")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AIChallengeContentResponse> generateChallengeContent(
            Authentication authentication,
            @Valid @RequestBody AIChallengeGenerateRequest request) {

        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);

        LessonRequest generatedLesson = aiLessonGeneratorService.generateLesson(
                accountId,
                request.getTopicId(),
                request.getLessonType(),
                request.getDescription(),
                request.getItemCount()
        );

        AIChallengeContentResponse response = challengeBuilder.toAIChallengeContentResponse(generatedLesson);

        return ResponseEntity.ok(response);
    }
}
