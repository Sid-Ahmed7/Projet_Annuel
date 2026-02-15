package com.glotrush.controllers;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.glotrush.dto.request.LanguageRequest;
import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.response.ApiResponse;
import com.glotrush.dto.response.LessonResponse;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.glotrush.dto.response.LanguageResponse;
import com.glotrush.services.languages.ILanguageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/languages")
@RequiredArgsConstructor
public class LanguageController {
    private final MessageSource messageSource;
    private final ILanguageService languageService;

    protected final Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    @GetMapping
    public ResponseEntity<List<LanguageResponse>> getAllLanguages() {
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

    /* PARTIE ADMINISTRATEUR */
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
        return ResponseEntity.ok(new ApiResponse(messageSource.getMessage("info.language.deleted_successfully", null, getCurrentLocale())));
    }
}
