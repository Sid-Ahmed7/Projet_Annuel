package com.glotrush.dto.response;

import java.time.LocalDateTime;
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
public class TopicProgressResponse {
    private UUID id;
    private String name;
    private String description;
    private ProficiencyLevel difficulty;
    private double progressPercent;
    private LocalDateTime lastStudiedAt;
}
