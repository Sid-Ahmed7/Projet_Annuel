package com.glotrush.services.topic;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.request.TopicRequest;
import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.TopicResponse;

public interface ITopicService {
    
    List<TopicResponse> getAllTopics(UUID accountId);
    List<TopicResponse> getAllTopics();
    List<TopicResponse> getTopicsByLanguage(UUID languageId, UUID accountId);
    TopicResponse getTopicById(UUID topicId, UUID accountId);
    TopicResponse createTopic(TopicRequest topic);
    void removeTopic(UUID topicId);
    TopicResponse updateTopic(UUID topicId, TopicRequest topic);
}
