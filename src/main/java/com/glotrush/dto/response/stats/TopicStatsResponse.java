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
public class TopicStatsResponse {
    private UUID topicId;
    private String topicName;
    private String languageName;
    private Double completionPercentage;
    private Long totalXP;
    private Integer completedLessons;
    private Integer totalAnswers;
    private Integer correctAnswers;
    private Double accuracy;
    private Long studyTimeSeconds;
    private LocalDateTime lastStudiedAt;
}