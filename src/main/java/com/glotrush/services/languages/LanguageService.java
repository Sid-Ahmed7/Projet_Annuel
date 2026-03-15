package com.glotrush.services.languages;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.request.LanguageRequest;
import com.glotrush.exceptions.LessonNotFoundException;
import com.glotrush.mapping.LanguageMapper;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.glotrush.builder.LanguageBuilder;
import com.glotrush.dto.response.LanguageResponse;
import com.glotrush.entities.Language;
import com.glotrush.exceptions.LanguageException;
import com.glotrush.repositories.LanguageRepository;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LanguageService implements ILanguageService {
    private final MessageSource messageSource;
    private final LanguageRepository languageRepository;
    private final LanguageBuilder languageBuilder;
    private final LanguageMapper languageMapper;



    @Override
    @Transactional
    public List<LanguageResponse> getAllActiveLanguages() {
        return languageRepository.findByIsActiveTrueOrderByOrderIndexAsc().stream().map(languageBuilder::mapToLanguageResponse).toList();
    }

    @Override
    @Transactional
    public List<LanguageResponse> getAllLanguages() {
        return languageRepository.findAll().stream().map(languageBuilder::mapToLanguageResponse).toList();
    }

    @Override
    @Transactional
    public LanguageResponse getLanguageById(UUID id) {
        Language language = languageRepository.findById(id)
           .orElseThrow(() -> new LanguageException(messageSource.getMessage("error.language.not_found", null, LocaleUtils.getCurrentLocale())));
        return languageBuilder.mapToLanguageResponse(language);

    }

    @Override
    public LanguageResponse getLanguageByCode(String code) {
        Language language = languageRepository.findByCode(code)
                .orElseThrow(() -> new LanguageException(messageSource.getMessage("error.language.not_found", null, LocaleUtils.getCurrentLocale())));
        return languageBuilder.mapToLanguageResponse(language); 
    }

    @Override
    public void removeLanguage(UUID languageId) {
        if (!languageRepository.existsById(languageId)) {
            throw new LessonNotFoundException(messageSource.getMessage("error.language.not_found", null, LocaleUtils.getCurrentLocale()));
        }
        languageRepository.deleteById(languageId);
    }

    @Override
    public LanguageResponse updateLanguage(UUID languageId, LanguageRequest languageRequest) {
        Language language = languageRepository.findById(languageId)
                .orElseThrow(() -> new LessonNotFoundException(messageSource.getMessage("error.language.not_found", null, LocaleUtils.getCurrentLocale())));
        
        languageMapper.updateLanguageFromRequest(languageRequest, language);
        
        languageRepository.save(language);
        return languageMapper.mapLanguageEntitiesToLanguageResponse(language);
    }

    @Override
    public LanguageResponse createLanguage(LanguageRequest languageRequest) {
        Language language = languageMapper.mapLanguageRequestToMapLanguageEntities(languageRequest);
        languageRepository.save(language);
        return languageMapper.mapLanguageEntitiesToLanguageResponse(language);

    }
}
