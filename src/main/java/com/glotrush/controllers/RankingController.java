package com.glotrush.controllers;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.response.RankingResponse;
import com.glotrush.services.ranking.IRankingService;
import com.glotrush.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final IRankingService rankingService;

    @GetMapping("/global")
    public ResponseEntity<RankingResponse> getGlobalRanking(Authentication authentication, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "20") Integer size) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(rankingService.getGlobalRanking(accountId, page, size));
    }

    @GetMapping("/language/{languageId}")
    public ResponseEntity<RankingResponse> getLanguageRanking(Authentication authentication, @PathVariable UUID languageId, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "20") Integer size) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(rankingService.getLanguageRanking(accountId, languageId, page, size));
    }
}
