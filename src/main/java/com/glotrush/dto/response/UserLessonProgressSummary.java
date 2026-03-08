package com.glotrush.dto.response;

import java.time.LocalDateTime;

import com.glotrush.enumerations.LessonStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLessonProgressSummary {
    private LessonStatus status;
    private Integer totalAttempts;
    private Integer failedAttempts;
    private Double score;
    private Integer timeSpentSeconds;
    private LocalDateTime completedAt;
    private LocalDateTime lastAttemptAt;
}
