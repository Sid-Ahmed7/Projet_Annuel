package com.glotrush.services.topic;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.glotrush.dto.request.TopicRequest;
import com.glotrush.entities.Language;
import com.glotrush.enumerations.ProficiencyLevel;
import com.glotrush.mapping.TopicMapper;
import com.glotrush.repositories.LanguageRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.glotrush.entities.Topic;
import com.glotrush.entities.UserProgress;
import com.glotrush.exceptions.TopicNotFoundException;
import com.glotrush.builder.TopicBuilder;
import com.glotrush.dto.response.TopicResponse;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.UserProgressRepository;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;


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
    public List<TopicResponse> getAllTopics() {
        return topicRepository.findAll().stream()
                .map(topicMapper::mapTopicEntitiesToTopicResponse)
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
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale())));
        Optional<UserProgress> progressOpt = userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId);
        return topicBuilder.mapToTopicResponse(topic, progressOpt);
    }

    @Override
    public TopicResponse createTopic(TopicRequest topicRequest) {
        Language language = languageRepository.findById(topicRequest.getLanguageId())
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.language_notfound", null, LocaleUtils.getCurrentLocale())));
        
        Topic topicEntity = topicMapper.mapTopicRequestToMapTopicEntities(topicRequest);
        topicEntity.setLanguage(language);
        
        topicRepository.save(topicEntity);
        return topicMapper.mapTopicEntitiesToTopicResponse(topicEntity);
    }

    @Override
    public void removeTopic(UUID topicId) {
        if(!topicRepository.existsById(topicId)) {
            throw new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale()));
        }
        topicRepository.deleteById(topicId);
    }

    @Override
    public TopicResponse updateTopic(UUID topicId, TopicRequest topicRequest) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale())));

        if (topicRequest.getLanguageId() != null && !topicRequest.getLanguageId().equals(topic.getLanguage().getId())) {
            Language language = languageRepository.findById(topicRequest.getLanguageId())
                    .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.language_notfound", null, LocaleUtils.getCurrentLocale())));
            topic.setLanguage(language);
        }

        topicMapper.updateTopicFromRequest(topicRequest, topic);
        
        return topicMapper.mapTopicEntitiesToTopicResponse(topicRepository.save(topic));
    }

    @Override
    public List<TopicResponse> searchTopics(String name, ProficiencyLevel difficulty, Boolean isActive) {
        Specification<Topic> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (difficulty != null) {
                predicates.add(criteriaBuilder.equal(root.get("difficulty"), difficulty));
            }

            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return topicRepository.findAll(spec).stream()
                .map(topicMapper::mapTopicEntitiesToTopicResponse)
                .toList();
    }

}
