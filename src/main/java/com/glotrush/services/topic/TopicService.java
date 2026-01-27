package com.glotrush.services.topic;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    private final TopicRepository topicRepository;
    private final UserProgressRepository userProgressRepository;
    private final TopicBuilder topicBuilder;

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
                .orElseThrow(() -> new TopicNotFoundException("Topic not found"));
        Optional<UserProgress> progressOpt = userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId);
        return topicBuilder.mapToTopicResponse(topic, progressOpt);
    }
    
}
