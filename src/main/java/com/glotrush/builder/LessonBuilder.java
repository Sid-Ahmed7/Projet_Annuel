package com.glotrush.builder;

import java.util.Optional;

import com.glotrush.utils.LevelUtils;
import org.springframework.stereotype.Component;

import com.glotrush.dto.response.CompleteLessonResponse;
import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.UserLessonProgressSummary;
import com.glotrush.dto.response.UserProgressResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.UserLessonProgress;
import com.glotrush.entities.UserProgress;
import com.glotrush.enumerations.LessonStatus;

@Component
public class LessonBuilder {

    public LessonResponse mapLessonToLessonResponse(Lesson lesson, Optional<UserLessonProgress> progressOpt, String content) {
        UserLessonProgressSummary progressSummary = progressOpt
                .map(this::mapToUserLessonProgressSummary)
                .orElse(null);

        return LessonResponse.builder()
                .id(lesson.getId())
                .topicId(lesson.getTopic().getId())
                .topicName(lesson.getTopic().getName())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .orderIndex(lesson.getOrderIndex())
                .xpReward(lesson.getXpReward())
                .minLevelRequired(lesson.getMinLevelRequired())
                .durationMinutes(lesson.getDurationMinutes())
                .passScorePercentage(lesson.getPassScorePercentage())
                .isActive(lesson.getIsActive())
                .userProgress(progressSummary)
                .build();
    }

    public UserLessonProgressSummary mapToUserLessonProgressSummary(UserLessonProgress progress) {
        return UserLessonProgressSummary.builder()
                .status(progress.getStatus())
                .totalAttempts(progress.getTotalAttempts())
                .failedAttempts(progress.getFailedAttempts())
                .score(progress.getScore())
                .timeSpentSeconds(progress.getTimeSpentSeconds())
                .lastAttemptAt(progress.getLastAttemptAt())
                .build();
    }

    public UserLessonProgress createNewLessonProgress(Accounts account, Lesson lesson) {
        return UserLessonProgress.builder()
                .account(account)
                .lesson(lesson)
                .status(LessonStatus.NOT_STARTED)
                .totalAttempts(0)
                .timeSpentSeconds(0)
                .build();
    }

    public CompleteLessonResponse buildCompleteLessonResponse(
            boolean leveledUp,
            Integer xpEarned,
            UserProgress topicProgress,
            UserProgressResponse progressResponse,
            Integer newLevel) {

        return CompleteLessonResponse.builder()
                .success(true)
                .message(leveledUp ? "Congratulations! You leveled up!" : "Lesson completed successfully!")
                .xpEarned(xpEarned)
                .totalXP(topicProgress.getTotalXP())
                .currentLevel(LevelUtils.calculateLevel(topicProgress.getTotalXP()))
                .leveledUp(leveledUp)
                .newLevel(leveledUp ? newLevel : null)
                .progress(progressResponse)
                .build();
    }

    public CompleteLessonResponse buildRecompletedLessonResponse(UserProgressResponse progressResponse) {
        return CompleteLessonResponse.builder()
                .success(true)
                .message("Lesson re-completed. Score updated.")
                .xpEarned(0)
                .totalXP(progressResponse.getTotalXP())
                .currentLevel(LevelUtils.calculateLevel(progressResponse.getTotalXP()))
                .leveledUp(false)
                .progress(progressResponse)
                .build();
    }
}
