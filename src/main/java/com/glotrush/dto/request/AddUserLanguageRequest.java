package com.glotrush.dto.request;

import java.util.UUID;

import com.glotrush.enumerations.LanguageType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @NotNull(message = "Language ID is required")
    private UUID languageId;

    @NotNull(message = "Language type is required")
    private LanguageType languageType;

    @Min(value = 1, message = "Proficiency level must be between 1 and 6")
    @Max(value = 6, message = "Proficiency level must be between 1 and 6")
    private Integer proficiencyLevel;

    private Boolean isPrimary = false;
}
