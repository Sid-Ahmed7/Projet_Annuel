package com.glotrush.dto.response;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {

    private UUID userId;
    private String email;
    private String username;
    private String displayName;
    private String role;

    private Long totalXP;
    private Integer overallLevel;
    private Integer totalLessonsCompleted;
    private Integer totalTopicsStarted;
    private Long totalTimeSpentSeconds;
    private Double overallAccuracy;
    private Integer currentStreak;

    private List<UserProgressResponse> progressByTopic;
}
