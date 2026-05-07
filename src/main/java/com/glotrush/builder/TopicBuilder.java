package com.glotrush.builder;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.TopicResponse;
import com.glotrush.dto.response.TopicWithProgressResponse;
import com.glotrush.entities.Topic;
import com.glotrush.entities.UserProgress;

@Component
public class TopicBuilder {

    public TopicResponse mapToTopicResponse(Topic topic, Optional<UserProgress> progressOpt) {
        return TopicResponse.builder()
                .id(topic.getId())
                .targetLanguageId(topic.getTargetLanguage().getId())
                .sourceLanguageId(topic.getSourceLanguage().getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .difficulty(topic.getDifficulty())
                .isActive(topic.getIsActive())
                .build();
    }

    public TopicWithProgressResponse mapToTopicWithProgressResponse(Topic topic, List<LessonResponse> lessons, int completedLessons, boolean examUnlocked, boolean examPassed) {
        int totalLessons = lessons.size();
        int progressPercent = totalLessons > 0 ? (completedLessons * 100 / totalLessons) : 0;

        return TopicWithProgressResponse.builder()
                .id(topic.getId())
                .targetLanguageId(topic.getTargetLanguage().getId())
                .sourceLanguageId(topic.getSourceLanguage().getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .difficulty(topic.getDifficulty())
                .isActive(topic.getIsActive())
                .completedLessons(completedLessons)
                .totalLessons(totalLessons)
                .progressPercent(progressPercent)
                .lessons(lessons)
                .examUnlocked(examUnlocked)
                .examCompleted(examPassed)
                .build();
    }
}
