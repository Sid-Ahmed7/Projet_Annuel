package com.glotrush.dto.request;

import java.util.UUID;

import com.glotrush.enumerations.LessonSessionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonSessionRequest {

    private UUID accountId;
    private UUID lessonId;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Integer totalQuestions;
    private Integer totalTime;
    private LessonSessionStatus status;
    
}
