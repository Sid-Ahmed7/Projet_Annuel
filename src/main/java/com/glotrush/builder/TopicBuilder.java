package com.glotrush.builder;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.glotrush.dto.response.TopicResponse;
import com.glotrush.dto.response.UserProgressSummary;
import com.glotrush.entities.Topic;
import com.glotrush.entities.UserProgress;

@Component
public class TopicBuilder {

    public TopicResponse mapToTopicResponse(Topic topic, Optional<UserProgress> progressOpt) {
        UserProgressSummary progressSummary = null;

        if (progressOpt.isPresent()) {
            UserProgress p = progressOpt.get();
            progressSummary = UserProgressSummary.builder()
                    .level(p.getLevel())
                    .totalXP(p.getTotalXP())
                    .completedLessons(p.getCompletedLessons())
                    .completionPercentage(p.getCompletionPercentage())
                    .accuracy(p.getAccuracy())
                    .build();
        }

        return TopicResponse.builder()
                .id(topic.getId())
                .languageId(topic.getLanguage().getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .difficulty(topic.getDifficulty())
                .totalLessons(topic.getTotalLessons())
                .orderIndex(topic.getOrderIndex())
                .isActive(topic.getIsActive())
                .userProgress(progressSummary)
                .build();
    }

    public UserProgressSummary buildUserProgressSummary(UserProgress progress) {
        return UserProgressSummary.builder()
                .level(progress.getLevel())
                .totalXP(progress.getTotalXP())
                .completedLessons(progress.getCompletedLessons())
                .completionPercentage(progress.getCompletionPercentage())
                .accuracy(progress.getAccuracy())
                .build();
    }
}
