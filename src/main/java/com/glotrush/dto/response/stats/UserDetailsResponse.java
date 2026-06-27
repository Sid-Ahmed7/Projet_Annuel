package com.glotrush.dto.response.stats;

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
public class UserDetailsResponse {
    private UUID accountId;
    private String username;
    private String email;
    private String photoUrl;
    private String status;
    private Integer currentStreak;
    private Integer longestStreak;
    private Long totalXP;
    private Long totalLessonsCompleted;
    private Double avgAccuracy;
    private Long totalStudyTimeMinutes;
    private Long topicsCompleted;
    private List<TopicStatsResponse> topicStats;
}
