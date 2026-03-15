package com.glotrush.dto.response;

import java.util.UUID;

import com.glotrush.enumerations.ProficiencyLevel;

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
    private String name;
    private String description;
    private ProficiencyLevel difficulty;
    private Integer orderIndex;
    private Boolean isActive;
}
