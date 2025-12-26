package com.glotrush.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.glotrush.enumerations.LanguageType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLanguageResponse {
    private UUID id;
    private UUID languageId;
    private String languageCode;
    private String languageName;
    private String flagIcon;
    private LanguageType languageType;
    private Integer proficiencyLevel;
    private Boolean isPrimary;
    private LocalDateTime startedAt;
    
}
