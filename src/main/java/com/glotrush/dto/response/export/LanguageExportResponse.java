package com.glotrush.dto.response.export;

import java.time.LocalDateTime;

import com.glotrush.enumerations.LanguageType;
import com.glotrush.enumerations.ProficiencyLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageExportResponse {
    private String language;
    private LanguageType type;
    private ProficiencyLevel level;
    private LocalDateTime startedAt;
}
