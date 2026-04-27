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
public class LanguageLevelResponse {
    private UUID languageId;
    private String languageName;
    private String languageCode;
    private Integer level;
    private Long totalXP;
    private Long currentLevelXP;
    private Long nextLevelXP;
    private Double levelProgressPercentage;
}
