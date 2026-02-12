package com.glotrush.dto.request;

import com.glotrush.dto.response.UserProgressSummary;
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
    @Min(1)
    @Max(10)
    private Integer difficulty;

    @NotNull
    @Min(0)
    private Integer totalLessons;

    @NotNull
    @Min(0)
    private Integer orderIndex;

    @NotNull
    private Boolean isActive;

    @NotNull
    @Min(0)
    private Integer minLevelRequired;

    @NotNull
    private UserProgressSummary userProgress;

}
