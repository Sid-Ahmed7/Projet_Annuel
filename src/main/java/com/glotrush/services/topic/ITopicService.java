package com.glotrush.services.topic;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.request.ExamResultRequest;
import com.glotrush.dto.request.TopicRequest;
import com.glotrush.dto.response.CompleteExamResponse;
import com.glotrush.dto.response.ExamResponse;
import com.glotrush.dto.response.TopicResponse;
import com.glotrush.dto.response.TopicWithProgressResponse;
import com.glotrush.enumerations.ProficiencyLevel;

public interface ITopicService {

    List<TopicResponse> getAllTopics(UUID accountId);
    List<TopicResponse> getAllTopics();
    List<TopicWithProgressResponse> getTopicsByLanguage(UUID languageId, UUID accountId);
    TopicResponse getTopicById(UUID topicId, UUID accountId);
    TopicResponse createTopic(TopicRequest topic);
    void removeTopic(UUID topicId);
    TopicResponse updateTopic(UUID topicId, TopicRequest topic);
    List<TopicResponse> searchTopics(String name, ProficiencyLevel difficulty, Boolean isActive);
    List<TopicResponse> searchActiveTopics(UUID languageId, String name, ProficiencyLevel difficulty);

    ExamResponse generateTopicExam(UUID accountId, UUID topicId);
    CompleteExamResponse completeTopicExam(UUID accountId, UUID topicId, ExamResultRequest examRequest);
}
