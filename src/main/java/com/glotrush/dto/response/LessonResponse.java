package com.glotrush.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponse {
    private UUID id;
    private UUID topicId;
    private String topicName;
    private String title;
    private String description;
    private String content;
    private Integer orderIndex;
    private Integer xpReward;
    private Boolean isLocked;
    private Integer minLevelRequired;
    private Integer durationMinutes;
    private Boolean isActive;

    private UserLessonProgressSummary userProgress;
}
