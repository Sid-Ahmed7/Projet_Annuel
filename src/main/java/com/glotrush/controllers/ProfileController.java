package com.glotrush.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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

import com.glotrush.dto.request.PasswordRequest;
import com.glotrush.dto.request.UpdateProfileRequest;
import com.glotrush.dto.response.ApiResponse;
import com.glotrush.dto.response.UploadResponse;
import com.glotrush.dto.response.UserProfileResponse;
import com.glotrush.services.images.IProfileService;
import com.glotrush.services.userprofile.IUserProfileService;
import com.glotrush.utils.LocaleUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final IUserProfileService profileService;
    private final IProfileService profileImageService;
    private final MessageSource messageSource;
    private static final String IMAGE_BASE_URL = "/api/v1/profile/files/";

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

    @PutMapping("/password")
    public ResponseEntity<ApiResponse> changePassword(Authentication authentication, @Valid @RequestBody PasswordRequest request) {
        UUID accountId = UUID.fromString(authentication.getName());
        profileService.changePassword(accountId, request);
        return ResponseEntity.ok(new ApiResponse(messageSource.getMessage("success.password.changed", null, LocaleUtils.getCurrentLocale())));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadProfileImage(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        UUID accountId = UUID.fromString(authentication.getName());
        String imageUrl =  IMAGE_BASE_URL + profileImageService.uploadImage(accountId, file);

        return ResponseEntity.ok(UploadResponse.builder()
            .message(messageSource.getMessage("success.image.uploaded", null, LocaleUtils.getCurrentLocale()))
            .pathFile(imageUrl)
            .build());
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> getProfileImage(@PathVariable String filename) throws IOException {
        Path filePath = profileImageService.getImagePath(filename);

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
            .body(resource);
    }

    @DeleteMapping("/delete/image")
    public ResponseEntity<ApiResponse> deleteProfileImage(Authentication authentication) {
        UUID accountId = UUID.fromString(authentication.getName());
        profileImageService.deleteImage(accountId);
        return ResponseEntity.ok(new ApiResponse(messageSource.getMessage("success.image.deleted", null, LocaleUtils.getCurrentLocale())));
    }
}

