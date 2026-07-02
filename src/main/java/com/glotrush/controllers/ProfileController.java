package com.glotrush.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.glotrush.constants.ImageConstants;
import com.glotrush.dto.request.NotificationPreferencesRequest;
import com.glotrush.dto.request.PasswordRequest;
import com.glotrush.dto.request.UpdateProfileRequest;
import com.glotrush.dto.response.ApiResponse;
import com.glotrush.dto.response.NotificationPreferencesResponse;
import com.glotrush.dto.response.StreakResponse;
import com.glotrush.dto.response.UploadResponse;
import com.glotrush.dto.response.UserProfileResponse;
import com.glotrush.services.images.IProfileService;
import com.glotrush.services.userprofile.IUserProfileService;
import com.glotrush.storage.FileStorageService;
import com.glotrush.utils.LocaleUtils;
import com.glotrush.utils.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final IUserProfileService profileService;
    private final IProfileService profileImageService;
    private final FileStorageService fileStorageService;
    private final MessageSource messageSource;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        UserProfileResponse profile = profileService.getProfile(accountId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateMyProfile(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        UserProfileResponse profile = profileService.updateProfile(accountId, request);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable UUID userId, Authentication authentication) {
        UUID viewerAccountId = authentication != null ? SecurityUtils.extractUserIdFromAuth(authentication) : null;
        UserProfileResponse profile = profileService.getPublicProfile(userId, viewerAccountId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse> changePassword(Authentication authentication, @Valid @RequestBody PasswordRequest request) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        profileService.changePassword(accountId, request);
        return ResponseEntity.ok(new ApiResponse(messageSource.getMessage("success.password.changed", null, LocaleUtils.getCurrentLocale())));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadProfileImage(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        String imageUrl = ImageConstants.IMAGE_BASE_URL + profileImageService.uploadImage(accountId, file);
        return ResponseEntity.ok(UploadResponse.builder()
                .message(messageSource.getMessage("success.image.uploaded", null, LocaleUtils.getCurrentLocale()))
                .pathFile(imageUrl)
                .build());
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> getProfileImage(@PathVariable String filename) throws IOException {
        String imageKey = profileImageService.getImageKey(filename);
        InputStream stream = fileStorageService.download(imageKey);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(detectContentType(filename)))
                .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
                .body(new InputStreamResource(stream));
    }

    @PutMapping("/active-language/{languageId}")
    public ResponseEntity<UserProfileResponse> setActiveLanguage(Authentication authentication, @PathVariable UUID languageId) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        UserProfileResponse profile = profileService.addActiveLanguage(accountId, languageId);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/delete/image")
    public ResponseEntity<ApiResponse> deleteProfileImage(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        profileImageService.deleteImage(accountId);
        return ResponseEntity.ok(new ApiResponse(messageSource.getMessage("success.image.deleted", null, LocaleUtils.getCurrentLocale())));
    }

    @GetMapping("/streak")
    public ResponseEntity<StreakResponse> getStreak(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        StreakResponse streakResponse = profileService.getStreak(accountId);
        return ResponseEntity.ok(streakResponse);
    }

    @GetMapping("/notification-preferences")
    public ResponseEntity<NotificationPreferencesResponse> getNotificationPreferences(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(profileService.getNotificationPreferences(accountId));
    }

    @PutMapping("/notification-preferences")
    public ResponseEntity<NotificationPreferencesResponse> updateNotificationPreferences(Authentication authentication, @RequestBody NotificationPreferencesRequest request) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        NotificationPreferencesResponse response = profileService.updateNotificationPreferences(accountId, request);
        return ResponseEntity.ok(response);
    }

    private String detectContentType(String filename) {
        String extension = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase() : "";
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}