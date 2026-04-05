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

    public LessonResponse mapLessonToLessonResponse(Lesson lesson, Optional<UserLessonProgress> progressOpt) {
        UserLessonProgressSummary userProgress = progressOpt
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
                .isActive(lesson.getIsActive())
                .lessonType(lesson.getLessonType())
                .userProgress(userProgress)
                .build();
    }

    public UserLessonProgressSummary mapToUserLessonProgressSummary(UserLessonProgress progress) {
        return UserLessonProgressSummary.builder()
                .status(progress.getStatus())
                .totalAttempts(progress.getTotalAttempts())
                .userFeedback(progress.getUserFeedback())
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
                .userFeedback(null)
                .build();
    }

    public CompleteLessonResponse buildCompleteLessonResponse(
            boolean leveledUp,
            Integer xpEarned,
            UserProgress topicProgress,
            UserProgressResponse progressResponse,
            Integer newLevel) {

        String message = leveledUp ? "Congratulations! You leveled up!" : (xpEarned > 0 ? "Great effort! You finished the lesson!" : "Lesson completed again! Keep it up!");
        int currentLevel = LevelUtils.calculateLevel(topicProgress.getTotalXP());
        return CompleteLessonResponse.builder()
                .success(true)
                .message(message)
                .xpEarned(xpEarned)
                .totalXP(topicProgress.getTotalXP())
                .currentLevel(leveledUp ? currentLevel - 1 : currentLevel)
                .leveledUp(leveledUp)
                .newLevel(leveledUp ? currentLevel : null)
                .progress(progressResponse)
                .build();
    }

}
