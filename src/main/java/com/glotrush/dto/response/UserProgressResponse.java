package com.glotrush.dto.response;

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
public class UserProgressResponse {
    
    private UUID id;
    private UUID accountId;
    private UUID topicId;
    private String topicName;
    private String languageCode;
    private String languageName;

    private Long totalXP;
    private Integer level;
    private Long currentLevelXP;
    private Long nextLevelXP;
    private Double levelProgressPercentage; 

    private Integer completedLessons;
    private Double completionPercentage;

    private Integer correctAnswers;
    private Integer totalAnswers;
    private Double accuracy;
    private Integer studyStreak;

    private LocalDateTime lastStudiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
