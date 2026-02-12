package com.glotrush.services.topic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import com.glotrush.dto.request.TopicRequest;
import com.glotrush.entities.Language;
import com.glotrush.mapping.TopicMapper;
import com.glotrush.repositories.LanguageRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.glotrush.entities.Topic;
import com.glotrush.entities.UserProgress;
import com.glotrush.exceptions.TopicNotFoundException;
import com.glotrush.builder.TopicBuilder;
import com.glotrush.dto.response.TopicResponse;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.UserProgressRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional
public class TopicService implements ITopicService {
    private final MessageSource messageSource;
    private final TopicRepository topicRepository;
    private final UserProgressRepository userProgressRepository;
    private final LanguageRepository languageRepository;
    private final TopicBuilder topicBuilder;
    private final TopicMapper topicMapper;

    protected final Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    @Override
    public List<TopicResponse> getAllTopics(UUID accountId) {
        return topicRepository.findByIsActiveTrueOrderByOrderIndexAsc().stream()
                .map(topic -> {
                    Optional<UserProgress> progressOpt = userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topic.getId());
                    return topicBuilder.mapToTopicResponse(topic, progressOpt);
                })
                .toList();
    }

    @Override
    public List<TopicResponse> getTopicsByLanguage(UUID languageId, UUID accountId) {
        return topicRepository.findByLanguage_IdAndIsActiveTrueOrderByOrderIndexAsc(languageId).stream().map(topic -> {
            Optional<UserProgress> progressOpt = userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topic.getId());
            return topicBuilder.mapToTopicResponse(topic, progressOpt);
        }).toList();
    }

    @Override
    public TopicResponse getTopicById(UUID topicId, UUID accountId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, getCurrentLocale())));
        Optional<UserProgress> progressOpt = userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId);
        return topicBuilder.mapToTopicResponse(topic, progressOpt);
    }

    @Override
    public TopicResponse createTopic(TopicRequest topicRequest) {
        Language language = languageRepository.findById(topicRequest.getLanguageId())
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.language_notfound", null, getCurrentLocale())));
        
        Topic topicEntity = topicMapper.mapTopicRequestToMapTopicEntities(topicRequest);
        topicEntity.setLanguage(language);
        
        topicRepository.save(topicEntity);
        return topicMapper.mapTopicEntitiesToTopicResponse(topicEntity);
    }

    @Override
    public void removeTopic(UUID topicId) {
        if(!topicRepository.existsById(topicId)) {
            throw new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, getCurrentLocale()));
        }
        topicRepository.deleteById(topicId);
    }

    @Override
    public TopicResponse updateTopic(UUID topicId, TopicRequest topicRequest) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, getCurrentLocale())));

        if (topicRequest.getLanguageId() != null && !topicRequest.getLanguageId().equals(topic.getLanguage().getId())) {
            Language language = languageRepository.findById(topicRequest.getLanguageId())
                    .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.language_notfound", null, getCurrentLocale())));
            topic.setLanguage(language);
        }

        topicMapper.updateTopicFromRequest(topicRequest, topic);
        
        return topicMapper.mapTopicEntitiesToTopicResponse(topicRepository.save(topic));
    }

}
