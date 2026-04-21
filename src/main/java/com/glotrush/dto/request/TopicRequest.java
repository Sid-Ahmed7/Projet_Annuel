package com.glotrush.dto.request;

import com.glotrush.enumerations.ProficiencyLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicRequest {
    @NotNull
    private UUID languageId;

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    private ProficiencyLevel difficulty;

    @NotNull
    private Boolean isActive;
}
