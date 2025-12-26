package com.glotrush.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.request.AddUserLanguageRequest;
import com.glotrush.dto.request.UpdateUserLanguageRequest;
import com.glotrush.dto.response.ApiResponse;
import com.glotrush.dto.response.UserLanguageResponse;
import com.glotrush.enumerations.LanguageType;
import com.glotrush.services.languages.IUserLanguageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/user-languages")
@RequiredArgsConstructor
public class UserLanguageController {

    private final IUserLanguageService userLanguageService;

    @GetMapping
    public ResponseEntity<List<UserLanguageResponse>> getMyLanguages(Authentication authentication) {
        UUID accountId = UUID.fromString(authentication.getName());
        List<UserLanguageResponse> languages = userLanguageService.getUserLanguages(accountId);
        return ResponseEntity.ok(languages);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<UserLanguageResponse>> getMyLanguagesByType(Authentication authentication,@PathVariable LanguageType type) {
        UUID accountId = UUID.fromString(authentication.getName());
        List<UserLanguageResponse> languages = userLanguageService.getUserLanguagesByType(accountId, type);
        return ResponseEntity.ok(languages);
    }

    @PostMapping
    public ResponseEntity<UserLanguageResponse> addLanguage(Authentication authentication,@Valid @RequestBody AddUserLanguageRequest request) {
        UUID accountId = UUID.fromString(authentication.getName());
        UserLanguageResponse language = userLanguageService.addLanguage(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(language);
    }

    @PutMapping("/{languageId}")
    public ResponseEntity<UserLanguageResponse> updateLanguage(Authentication authentication,@PathVariable UUID languageId,@Valid @RequestBody UpdateUserLanguageRequest request) {
        UUID accountId = UUID.fromString(authentication.getName());
        UserLanguageResponse language = userLanguageService.updateLanguage(accountId, languageId, request);
        return ResponseEntity.ok(language);
    }

    @DeleteMapping("/{languageId}")
    public ResponseEntity<ApiResponse> removeLanguage(Authentication authentication,@PathVariable UUID languageId) {
        UUID accountId = UUID.fromString(authentication.getName());
        userLanguageService.removeLanguage(accountId, languageId);
        return ResponseEntity.ok(new ApiResponse("Language removed successfully"));
    }
}
