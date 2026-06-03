package com.glotrush.controllers.admin;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.request.ai.AILessonGenerateRequest;
import com.glotrush.dto.request.ai.AILessonModifyRequest;
import com.glotrush.services.ai.AILessonGeneratorService;
import com.glotrush.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/lessons")
@RequiredArgsConstructor
public class AdminAILessonController {

    private final AILessonGeneratorService aiLessonGeneratorService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LessonRequest> generateLesson(
            Authentication authentication,
            @Valid @RequestBody AILessonGenerateRequest request) {
        
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        
        LessonRequest generatedLesson = aiLessonGeneratorService.generateLesson(
                accountId,
                request.getTopicId(),
                request.getLessonType(),
                request.getDescription(),
                request.getItemCount()
        );
        
        return ResponseEntity.ok(generatedLesson);
    }

    @PostMapping("/modify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LessonRequest> modifyLesson(
            Authentication authentication,
            @Valid @RequestBody AILessonModifyRequest request) {
        
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        
        LessonRequest modifiedLesson = aiLessonGeneratorService.modifyLesson(
                accountId,
                request.getLessonId(),
                request.getPrompt(),
                request.getItemCount(),
                request.getLesson()
        );
        
        return ResponseEntity.ok(modifiedLesson);
    }
}
