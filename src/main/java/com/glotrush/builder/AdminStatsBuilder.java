package com.glotrush.builder;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.glotrush.dto.response.stats.TopicStatsResponse;
import com.glotrush.dto.response.stats.UserDetailsResponse;
import com.glotrush.dto.response.stats.UserStatsResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.UserProfile;
import com.glotrush.entities.UserProgress;

@Component
public class AdminStatsBuilder {

    public UserStatsResponse buildUserStatsResponse(Accounts account, UserProfile profile, Long totalXP, Long totalLessons, Double avgAccuracy, Long studyTimeSeconds, Long topicsCompleted, LocalDateTime lastActivity) {
        return UserStatsResponse.builder()
            .accountId(account.getId())
            .username(account.getUsername())
            .email(account.getEmail())
            .photoUrl(profile != null ? profile.getPhotoUrl() : null)
            .status(account.getStatus().name())
            .totalXP(totalXP != null ? totalXP : 0L)
            .totalLessonsCompleted(totalLessons != null ? totalLessons : 0L)
            .avgAccuracy(avgAccuracy != null ? Math.round(avgAccuracy * 100 * 10.0) / 10.0 : 0.0)
            .totalStudyTimeMinutes(studyTimeSeconds != null ? studyTimeSeconds / 60 : 0L)
            .currentStreak(account.getCurrentStreak())
            .longestStreak(account.getLongestStreak())
            .topicsCompleted(topicsCompleted != null ? topicsCompleted : 0L)
            .lastActivityAt(lastActivity)
            .build();
    }

    public UserDetailsResponse buildUserDetailsResponse(Accounts account, UserProfile profile,Long totalXP, Long totalLessons, Double avgAccuracy,Long studyTimeSeconds, Long topicsCompleted,List<TopicStatsResponse> topicStats) {
        return UserDetailsResponse.builder()
            .accountId(account.getId())
            .username(account.getUsername())
            .email(account.getEmail())
            .photoUrl(profile != null ? profile.getPhotoUrl() : null)
            .status(account.getStatus().name())
            .currentStreak(account.getCurrentStreak())
            .longestStreak(account.getLongestStreak())
            .totalXP(totalXP != null ? totalXP : 0L)
            .totalLessonsCompleted(totalLessons != null ? totalLessons : 0L)
            .avgAccuracy(avgAccuracy != null ? Math.round(avgAccuracy * 100 * 10.0) / 10.0 : 0.0)
            .totalStudyTimeMinutes(studyTimeSeconds != null ? studyTimeSeconds / 60 : 0L)
            .topicsCompleted(topicsCompleted != null ? topicsCompleted : 0L)
            .topicStats(topicStats)
            .build();
    }

    public TopicStatsResponse buildTopicStatsResponse(UserProgress userProgress, Long studyTimeSeconds) {
        return TopicStatsResponse.builder()
            .topicId(userProgress.getTopic().getId())
            .topicName(userProgress.getTopic().getName())
            .languageName(userProgress.getTopic().getTargetLanguage().getName())
            .completionPercentage(userProgress.getCompletionPercentage())
            .totalXP(userProgress.getTotalXP())
            .completedLessons(userProgress.getCompletedLessons())
            .totalAnswers(userProgress.getTotalAnswers())
            .correctAnswers(userProgress.getCorrectAnswers())
            .accuracy(userProgress.getAccuracy() != null ? Math.round(userProgress.getAccuracy() * 100 * 10.0) / 10.0 : null)
            .studyTimeSeconds(studyTimeSeconds != null ? studyTimeSeconds : 0L)
            .lastStudiedAt(userProgress.getLastStudiedAt())
            .build();
    }
}
