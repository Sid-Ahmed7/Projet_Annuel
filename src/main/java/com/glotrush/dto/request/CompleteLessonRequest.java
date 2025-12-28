package com.glotrush.dto.request;

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
public class CompleteLessonRequest {
     @NotNull(message = "Score is required")
    @Min(value = 0, message = "Score must be between 0 and 100")
    @Max(value = 100, message = "Score must be between 0 and 100")
    private Double score;

    @NotNull(message = "Time spent is required")
    @Min(value = 0, message = "Time spent must be positive")
    private Integer timeSpentSeconds;

    private Integer correctAnswers;
    private Integer totalAnswers;
}
