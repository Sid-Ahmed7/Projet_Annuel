package com.glotrush.dto.response.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningStatsResponse {
    private Double avgAccuracy;
    private Double avgCompletionPercentage;
    private Long totalStudyTimeMinutes;
    private String mostPopularLanguage;
}