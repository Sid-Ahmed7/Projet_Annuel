package com.glotrush.dto.response;

import java.util.UUID;
import com.glotrush.enumerations.LessonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonSummaryResponse {
    private UUID id;
    private String title;
    private String description;
    private Integer orderIndex;
    private Integer xpReward;
    private Integer minLevelRequired;
    private Integer durationMinutes;
    private Boolean isActive;
    private LessonType lessonType;
    private Boolean isAlreadyFinish;
}
