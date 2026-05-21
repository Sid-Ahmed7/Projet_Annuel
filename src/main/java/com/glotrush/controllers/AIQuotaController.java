package com.glotrush.controllers;

import com.glotrush.dto.response.ai.AIQuotaResponse;
import com.glotrush.services.ai.AIQuotaService;
import com.glotrush.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIQuotaController {

    private final AIQuotaService aiQuotaService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/quota")
    public ResponseEntity<AIQuotaResponse> getMyQuota(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        AIQuotaResponse quotaResponse = aiQuotaService.getQuotaDetails(accountId);
        
        return ResponseEntity.ok(quotaResponse);
    }
}
