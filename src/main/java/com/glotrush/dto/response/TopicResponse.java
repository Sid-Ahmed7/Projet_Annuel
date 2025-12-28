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
public class TopicResponse {
    
    private UUID id;
    private UUID languageId;
    private String languageCode;
    private String languageName;
    private String name;
    private String description;
    private String icon;
    private Integer difficulty;
    private Integer totalLessons;
    private Integer orderIndex;
    private Boolean isActive;
    private Integer minLevelRequired;
    private UserProgressSummary userProgress;
}
