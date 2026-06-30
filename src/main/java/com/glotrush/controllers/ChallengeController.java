package com.glotrush.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.request.challenge.ChallengeResponseRequest;
import com.glotrush.dto.request.challenge.CreateChallengeRequest;
import com.glotrush.dto.response.challenge.ChallengeResponse;
import com.glotrush.dto.response.challenge.ChallengeUserResponse;
import com.glotrush.services.challenge.IChallengeService;
import com.glotrush.utils.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
public class ChallengeController {


    private final IChallengeService challengeService;


    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ChallengeResponse> createChallenge(Authentication authentication, @Valid @RequestBody CreateChallengeRequest request) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        ChallengeResponse response = challengeService.createChallenge(accountId, request);
        return ResponseEntity.ok(response);
    }       

    @PostMapping("/{challengeId}/join")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ChallengeResponse> joinChallenge(Authentication authentication, @PathVariable UUID challengeId) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        ChallengeResponse response = challengeService.joinChallenge(challengeId, accountId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{challengeId}/submit-result")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ChallengeResponse> submitChallengeResult(Authentication authentication, @PathVariable UUID challengeId, @Valid @RequestBody ChallengeResponseRequest request) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        ChallengeResponse response = challengeService.submitChallenge(challengeId, accountId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{challengeId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ChallengeResponse> getChallenge(Authentication authentication, @PathVariable UUID challengeId) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        ChallengeResponse response = challengeService.getChallenge(challengeId, accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-challenges")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ChallengeResponse>> getUserChallenges(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        List<ChallengeResponse> response = challengeService.getUserChallenge(accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-public-challenges")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ChallengeResponse>> getAllPublicChallenges(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        List<ChallengeResponse> response = challengeService.getPublicChallenges(accountId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{challengeId}/accept")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ChallengeResponse> acceptChallenge(Authentication authentication, @PathVariable UUID challengeId) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        ChallengeResponse response = challengeService.acceptChallenge(challengeId, accountId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{challengeId}/decline")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ChallengeResponse> declineChallenge(Authentication authentication, @PathVariable UUID challengeId) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        ChallengeResponse response = challengeService.declineChallenge(challengeId, accountId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{challengeId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ChallengeResponse> cancelChallenge(Authentication authentication, @PathVariable UUID challengeId) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        ChallengeResponse response = challengeService.cancelChallenge(challengeId, accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/other-participants")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ChallengeUserResponse>> getParticipants(Authentication authentication, @RequestParam UUID languageId) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        List<ChallengeUserResponse> response = challengeService.getChallengeUsers(languageId, accountId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{challengeId}/start")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> startChallenge(Authentication authentication, @PathVariable UUID challengeId) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        challengeService.startChallenge(challengeId, accountId);
        return ResponseEntity.ok().build();
    }
}