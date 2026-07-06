package com.glotrush.dto.response.export;

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
public class LessonProgressExportResponse {
    private String lesson;
    private LessonStatus status;
    private Integer totalAttempts;
    private Integer timeSpentSeconds;
    private LocalDateTime completedAt;
}
