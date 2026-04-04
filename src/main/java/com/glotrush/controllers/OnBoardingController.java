package com.glotrush.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.request.FirstLanguageRequest;
import com.glotrush.dto.response.ApiResponse;
import com.glotrush.services.onBoarding.IOnBoardingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnBoardingController {
    
    private final IOnBoardingService onBoardingService;

    @PostMapping("/complete")
    public ResponseEntity<ApiResponse> completeOnboarding(Authentication authentication, @RequestBody FirstLanguageRequest request) {
        UUID accountId = UUID.fromString(authentication.getName());
        onBoardingService.completeOnboarding(accountId, request);
        return ResponseEntity.ok(new ApiResponse("Onboarding completed successfully"));

    }
}
