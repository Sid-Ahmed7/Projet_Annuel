package com.glotrush.controllers;

import com.glotrush.dto.request.ai.AILessonHelpRequest;
import com.glotrush.dto.response.ai.AILessonHelpResponse;
import com.glotrush.services.ai.IAILessonHelpService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class AILessonHelpController {

    private final IAILessonHelpService aiLessonHelpService;

    @PostMapping("/help")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AILessonHelpResponse> getLessonHelp(
            Authentication authentication,
            @Valid @RequestBody AILessonHelpRequest request) {

        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);

        AILessonHelpResponse helpResponse = aiLessonHelpService.getLessonHelp(accountId, request);

        return ResponseEntity.ok(helpResponse);
    }
}
