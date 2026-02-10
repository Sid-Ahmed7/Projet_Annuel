package com.glotrush.services.languages;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.glotrush.dto.request.LanguageRequest;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.Topic;
import com.glotrush.exceptions.LessonNotFoundException;
import com.glotrush.exceptions.TopicNotFoundException;
import com.glotrush.mapping.LanguageMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private final MessageSource messageSource;
    private final LanguageRepository languageRepository;
    private final LanguageBuilder languageBuilder;
    private final LanguageMapper languageMapper;

    protected final Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    @Override
    @Transactional
    public List<LanguageResponse> getAllActiveLanguages() {
        return languageRepository.findByIsActiveTrueOrderByOrderIndexAsc().stream().map(languageBuilder::mapToLanguageResponse).toList();
    }

    @Override
    @Transactional
    public LanguageResponse getLanguageById(UUID id) {
        Language language = languageRepository.findById(id)
           .orElseThrow(() -> new LanguageException(messageSource.getMessage("error.language.not_found", null, getCurrentLocale())));
        return languageBuilder.mapToLanguageResponse(language);

    }

    @Override
    public LanguageResponse getLanguageByCode(String code) {
        Language language = languageRepository.findByCode(code)
                .orElseThrow(() -> new LanguageException(messageSource.getMessage("error.language.not_found", null, getCurrentLocale())));
        return languageBuilder.mapToLanguageResponse(language); 
    }

    @Override
    public void removeLanguage(UUID languageId) {
        if (!languageRepository.existsById(languageId)) {
            throw new LessonNotFoundException(messageSource.getMessage("error.language.not_found", null, getCurrentLocale()));
        }
        languageRepository.deleteById(languageId);
    }

    @Override
    public LanguageResponse updateLanguage(UUID languageId, LanguageRequest languageRequest) {
        Language languageOld = languageRepository.findById(languageId)
                .orElseThrow(() -> new LessonNotFoundException(messageSource.getMessage("error.language.not_found", null, getCurrentLocale())));
        Language language = languageMapper.mapLanguageRequestToMapLanguageEntities(languageRequest);
        language.setId(languageId);
        language.setCreatedAt(languageOld.getCreatedAt());
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
