package com.glotrush.dto.response.export;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonSessionExportResponse {
    private String lesson;          
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Integer totalTime;
    private LocalDateTime completedAt;
}
