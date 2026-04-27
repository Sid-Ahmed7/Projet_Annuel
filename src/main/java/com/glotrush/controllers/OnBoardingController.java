package com.glotrush.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.request.OnBoardingRequest;
import com.glotrush.dto.response.UserProfileResponse;
import com.glotrush.services.onBoarding.IOnBoardingService;
import com.glotrush.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnBoardingController {
    
    private final IOnBoardingService onBoardingService;

    @PostMapping("/complete")
    public ResponseEntity<UserProfileResponse> completeOnboarding(Authentication authentication, @RequestBody OnBoardingRequest request) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        UserProfileResponse userProfileResponse = onBoardingService.completeOnboarding(accountId, request);
        return ResponseEntity.ok(userProfileResponse);
    }
}
