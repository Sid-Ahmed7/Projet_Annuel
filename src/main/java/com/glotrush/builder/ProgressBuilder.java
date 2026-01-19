package com.glotrush.builder;

import java.util.List;

import org.springframework.stereotype.Component;

import com.glotrush.dto.response.ProgressOverviewResponse;
import com.glotrush.dto.response.UserProgressResponse;
import com.glotrush.entities.UserProgress;

@Component
public class ProgressBuilder {

    public ProgressOverviewResponse buildProgressOverview(
            Long totalXP,
            Integer overallLevel,
            Integer totalTopicsStarted,
            Integer totalLessonsCompleted,
            Double overallAccuracy,
            Integer currentStreak,
            List<UserProgressResponse> progressByTopic) {

        return ProgressOverviewResponse.builder()
                .totalXP(totalXP)
                .overallLevel(overallLevel)
                .totalTopicsStarted(totalTopicsStarted)
                .totalLessonsCompleted(totalLessonsCompleted)
                .overallAccuracy(overallAccuracy)
                .currentStreak(currentStreak)
                .progressByTopic(progressByTopic)
                .build();
    }

    public UserProgressResponse mapToUserProgressResponse(UserProgress progress) {
        Double levelProgressPercentage = calculateLevelProgressPercentage(
                progress.getCurrentLevelXP(),
                progress.getNextLevelXP()
        );

        return UserProgressResponse.builder()
                .id(progress.getId())
                .accountId(progress.getAccount().getId())
                .topicId(progress.getTopic().getId())
                .topicName(progress.getTopic().getName())
                .languageCode(progress.getTopic().getLanguage().getCode())
                .languageName(progress.getTopic().getLanguage().getName())
                .totalXP(progress.getTotalXP())
                .level(progress.getLevel())
                .currentLevelXP(progress.getCurrentLevelXP())
                .nextLevelXP(progress.getNextLevelXP())
                .levelProgressPercentage(levelProgressPercentage)
                .completedLessons(progress.getCompletedLessons())
                .totalLessons(progress.getTopic().getTotalLessons())
                .completionPercentage(progress.getCompletionPercentage())
                .correctAnswers(progress.getCorrectAnswers())
                .totalAnswers(progress.getTotalAnswers())
                .accuracy(progress.getAccuracy())
                .studyStreak(progress.getStudyStreak())
                .lastStudiedAt(progress.getLastStudiedAt())
                .createdAt(progress.getCreatedAt())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }

    private Double calculateLevelProgressPercentage(Long currentLevelXP, Long nextLevelXP) {
        if (nextLevelXP == null || nextLevelXP == 0) {
            return 0.0;
        }
        return (currentLevelXP.doubleValue() / nextLevelXP.doubleValue()) * 100.0;
    }
}
