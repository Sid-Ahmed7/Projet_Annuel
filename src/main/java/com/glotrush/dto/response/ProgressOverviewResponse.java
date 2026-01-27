package com.glotrush.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressOverviewResponse {
    private Long totalXP;
    private Integer overallLevel;
    private Integer totalTopicsStarted;
    private Integer totalLessonsCompleted;
    private Double overallAccuracy;
    private Integer currentStreak;
    private List<UserProgressResponse> progressByTopic;
}
