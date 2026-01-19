package com.glotrush.services.topic;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.response.TopicResponse;

public interface ITopicService {
    
    List<TopicResponse> getAllTopics(UUID accountId);
    List<TopicResponse> getTopicsByLanguage(UUID languageId, UUID accountId);
    TopicResponse getTopicById(UUID topicId, UUID accountId);

}
