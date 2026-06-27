package com.glotrush.dto.response.stats;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    private UUID accountId;
    private String username;
    private String email;
    private String photoUrl;
    private String status;
    private Long totalXP;
    private Long totalLessonsCompleted;
    private Double avgAccuracy;
    private Long totalStudyTimeMinutes;
    private Integer currentStreak;
    private Integer longestStreak;
    private Long topicsCompleted;
    private LocalDateTime lastActivityAt;
}
