package com.glotrush.services.languages;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.response.LanguageResponse;

public interface ILanguageService {
    List<LanguageResponse> getAllActiveLanguages();

    LanguageResponse getLanguageById(UUID id);

    LanguageResponse getLanguageByCode(String code);

}
