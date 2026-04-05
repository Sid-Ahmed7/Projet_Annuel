package com.glotrush.dto.request;

import com.glotrush.enumerations.DifficultyFeedback;
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
    @NotNull(message = "Time spent is required")
    @Min(value = 0, message = "Time spent must be positive")
    private Integer timeSpentSeconds;

    private DifficultyFeedback feedback;

    private Integer correctAnswers;
    private Integer totalAnswers;
}
