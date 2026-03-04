package com.glotrush.builder;

import java.util.Optional;

import com.glotrush.utils.LevelUtils;
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
                    .level(LevelUtils.calculateLevel(p.getTotalXP()))
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
                .orderIndex(topic.getOrderIndex())
                .isActive(topic.getIsActive())
                .userProgress(progressSummary)
                .build();
    }

    public UserProgressSummary buildUserProgressSummary(UserProgress progress) {
        return UserProgressSummary.builder()
                .level(LevelUtils.calculateLevel(progress.getTotalXP()))
                .totalXP(progress.getTotalXP())
                .completedLessons(progress.getCompletedLessons())
                .completionPercentage(progress.getCompletionPercentage())
                .accuracy(progress.getAccuracy())
                .build();
    }
}
