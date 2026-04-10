package com.glotrush.dto.request;

import java.util.UUID;

import com.glotrush.enumerations.LanguageType;
import com.glotrush.enumerations.ProficiencyLevel;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddUserLanguageRequest {
    @NotNull(message = "{error.languageId.required}")
    private UUID languageId;

    @NotNull(message = "{error.languageType.required}")
    private LanguageType languageType;

    private ProficiencyLevel proficiencyLevel;
}
