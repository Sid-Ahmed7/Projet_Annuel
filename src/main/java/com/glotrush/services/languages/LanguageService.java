package com.glotrush.services.languages;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.glotrush.builder.LanguageBuilder;
import com.glotrush.dto.response.LanguageResponse;
import com.glotrush.entities.Language;
import com.glotrush.exceptions.LanguageException;
import com.glotrush.repositories.LanguageRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LanguageService implements ILanguageService {

    private final LanguageRepository languageRepository;
    private final LanguageBuilder languageBuilder;

    @Override
    @Transactional
    public List<LanguageResponse> getAllActiveLanguages() {
        return languageRepository.findByIsActiveTrueOrderIndexAsc().stream().map(languageBuilder::mapToLanguageResponse).toList();
    }

    @Override
    @Transactional
    public LanguageResponse getLanguageById(UUID id) {
        Language language = languageRepository.findById(id)
           .orElseThrow(() -> new LanguageException("Language not found"));
        return languageBuilder.mapToLanguageResponse(language);

    }

    @Override
    public LanguageResponse getLanguageByCode(String code) {
        Language language = languageRepository.findByCode(code)
                .orElseThrow(() -> new LanguageException("Language not found"));
        return languageBuilder.mapToLanguageResponse(language); 
    }
}
