package com.glotrush.dto.request;

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
public class UpdateUserLanguageRequest {
    private LanguageType languageType;

    private ProficiencyLevel proficiencyLevel;
}
