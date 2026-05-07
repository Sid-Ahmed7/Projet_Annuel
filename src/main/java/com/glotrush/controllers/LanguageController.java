package com.glotrush.controllers;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.request.LanguageRequest;
import com.glotrush.dto.request.LessonReorderRequest;
import com.glotrush.dto.response.ApiResponse;
import com.glotrush.dto.response.LanguageResponse;
import com.glotrush.services.languages.ILanguageService;
import com.glotrush.utils.LocaleUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/languages")
@RequiredArgsConstructor
public class LanguageController {
    private final MessageSource messageSource;
    private final ILanguageService languageService;

    @GetMapping
    public ResponseEntity<List<LanguageResponse>> getAllLanguages() {
        List<LanguageResponse> languages = languageService.getAllLanguages();
        return ResponseEntity.ok(languages);
    }

    @GetMapping("/active")
    public ResponseEntity<List<LanguageResponse>> getAllActiveLanguages() {
        List<LanguageResponse> languages = languageService.getAllActiveLanguages();
        return ResponseEntity.ok(languages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LanguageResponse> getLanguageById(@PathVariable UUID id) {
        LanguageResponse language = languageService.getLanguageById(id);
        return ResponseEntity.ok(language);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<LanguageResponse> getLanguageByCode(@PathVariable String code) {
        LanguageResponse language = languageService.getLanguageByCode(code);
        return ResponseEntity.ok(language);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LanguageResponse> createLanguage(@Valid @RequestBody LanguageRequest languageRequest){
        LanguageResponse languageResponse = languageService.createLanguage(languageRequest);
        return ResponseEntity.ok(languageResponse);
    }

    @PutMapping("/{languageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LanguageResponse> updateLanguage(@PathVariable UUID languageId, @Valid @RequestBody LanguageRequest languageRequest){
        LanguageResponse languageResponse = languageService.updateLanguage(languageId, languageRequest);
        return ResponseEntity.ok(languageResponse);
    }

    @DeleteMapping("/{languageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteLanguage(@PathVariable UUID languageId){
        languageService.removeLanguage(languageId);
        return ResponseEntity.ok(new ApiResponse(messageSource.getMessage("info.language.deleted_successfully", null, LocaleUtils.getCurrentLocale())));
    }
}
