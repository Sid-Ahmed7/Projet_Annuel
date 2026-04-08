package com.glotrush.builder;

import java.util.List;

import com.glotrush.utils.LevelUtils;
import org.springframework.stereotype.Component;

import com.glotrush.dto.response.LanguageLevelResponse;
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
            Long currentLevelXP,
            Long nextLevelXP,
            Double levelProgressPercentage,
            List<UserProgressResponse> progressByTopic,
            List<LanguageLevelResponse> progressByLanguage) {

        return ProgressOverviewResponse.builder()
                .totalXP(totalXP)
                .overallLevel(overallLevel)
                .totalTopicsStarted(totalTopicsStarted)
                .totalLessonsCompleted(totalLessonsCompleted)
                .overallAccuracy(overallAccuracy)
                .currentStreak(currentStreak)
                .currentLevelXP(currentLevelXP)
                .nextLevelXP(nextLevelXP)
                .levelProgressPercentage(levelProgressPercentage)
                .progressByTopic(progressByTopic)
                .progressByLanguage(progressByLanguage)
                .build();
    }

    public UserProgressResponse mapToUserProgressResponse(UserProgress progress) {
        Long totalXP = progress.getTotalXP();
        Integer level = LevelUtils.calculateLevel(totalXP);
        Long currentLevelXP = LevelUtils.calculateCurrentLevelXP(totalXP);
        Long nextLevelXP = LevelUtils.calculateNextLevelXP((long) level);
        Double levelProgressPercentage = LevelUtils.calculateLevelProgressPercentage(totalXP);

        return UserProgressResponse.builder()
                .id(progress.getId())
                .accountId(progress.getAccount().getId())
                .topicId(progress.getTopic().getId())
                .topicName(progress.getTopic().getName())
                .languageCode(progress.getTopic().getLanguage().getCode())
                .languageName(progress.getTopic().getLanguage().getName())
                .totalXP(totalXP)
                .level(level)
                .currentLevelXP(currentLevelXP)
                .nextLevelXP(nextLevelXP)
                .levelProgressPercentage(levelProgressPercentage)
                .completedLessons(progress.getCompletedLessons())
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

}
