package com.glotrush.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.request.UpdateProfileRequest;
import com.glotrush.dto.response.UserProfileResponse;
import com.glotrush.services.userprofile.IUserProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final IUserProfileService profileService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {
        UUID accountId = UUID.fromString(authentication.getName());
        UserProfileResponse profile = profileService.getProfile(accountId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateMyProfile(Authentication authentication,@Valid @RequestBody UpdateProfileRequest request) {
        UUID accountId = UUID.fromString(authentication.getName());
        UserProfileResponse profile = profileService.updateProfile(accountId, request);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable UUID userId) {
        UserProfileResponse profile = profileService.getPublicProfile(userId);
        return ResponseEntity.ok(profile);
    }
}
