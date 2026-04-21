package com.glotrush.services.languages;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.glotrush.dto.request.LanguageRequest;
import com.glotrush.dto.request.LessonReorderRequest;
import com.glotrush.entities.Topic;
import com.glotrush.enumerations.LanguageType;
import com.glotrush.enumerations.ProficiencyLevel;
import com.glotrush.exceptions.LessonNotFoundException;
import com.glotrush.mapping.LanguageMapper;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.UserLanguageRepository;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.glotrush.builder.LanguageBuilder;
import com.glotrush.constants.LanguagesConstants;
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
    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;
    private final UserLanguageRepository userLanguageRepository;

    private LanguageResponse getLanguageDetails(Language language) {
        
        Set<UUID> popularLanguages = new HashSet<>(
            userLanguageRepository.findMostPopularLanguageIdsByLearnerCount(LanguageType.LEARNING, PageRequest.of(0, LanguagesConstants.POPULAR_LANGUAGES_LIMIT))
        );
        
        List<Topic> topics = topicRepository.findByLanguage_Id(language.getId());
        String levelRange = null;
        if (!topics.isEmpty()) {
            ProficiencyLevel min = topics.stream().map(Topic::getDifficulty).min(Comparator.naturalOrder()).orElse(null);
            ProficiencyLevel max = topics.stream().map(Topic::getDifficulty).max(Comparator.naturalOrder()).orElse(null);
            if (min != null && max != null) {
                levelRange = min.name() + " \u2192 " + max.name();
            }
        }
        int topicsCount = topics.size();
        int lessonsCount = lessonRepository.countByTopic_Language_Id(language.getId());
        boolean isPopular = popularLanguages.contains(language.getId());
        return languageBuilder.mapToLanguageResponse(language, levelRange, topicsCount, lessonsCount, isPopular);
    }

    @Override
    @Transactional
    public List<LanguageResponse> getAllActiveLanguages() {
        return languageRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(this::getLanguageDetails)
                .toList();
    }

    @Override
    @Transactional
    public List<LanguageResponse> getAllLanguages() {
        return languageRepository.findAll().stream()
                .map(this::getLanguageDetails)
                .toList();
    }

    @Override
    @Transactional
    public LanguageResponse getLanguageById(UUID id) {
        Language language = languageRepository.findById(id)
           .orElseThrow(() -> new LanguageException(messageSource.getMessage("error.language.not_found", null, LocaleUtils.getCurrentLocale())));
        return getLanguageDetails(language);
    }

    @Override
    public LanguageResponse getLanguageByCode(String code) {
        Language language = languageRepository.findByCode(code)
                .orElseThrow(() -> new LanguageException(messageSource.getMessage("error.language.not_found", null, LocaleUtils.getCurrentLocale())));
        return getLanguageDetails(language);
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
