package com.glotrush.services.languages;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.request.LanguageRequest;
import com.glotrush.dto.response.LanguageResponse;

import com.glotrush.dto.request.LessonReorderRequest;

public interface ILanguageService {
    List<LanguageResponse> getAllActiveLanguages();
    List<LanguageResponse> getAllLanguages();

    LanguageResponse getLanguageById(UUID id);

    LanguageResponse getLanguageByCode(String code);
    void removeLanguage(UUID languageId);
    LanguageResponse updateLanguage(UUID languageId, LanguageRequest languageRequest);
    LanguageResponse createLanguage(LanguageRequest languageRequest);
}
