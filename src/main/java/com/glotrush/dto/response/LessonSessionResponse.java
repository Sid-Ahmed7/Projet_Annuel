package com.glotrush.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.web.bind.support.SessionStatus;

import com.glotrush.enumerations.LessonSessionStatus;
import com.glotrush.enumerations.LessonType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonSessionResponse {
    
    private UUID id;
    private UUID lessonId;
    private String lessonTitle;
    private String topicName;
    private LessonType lessonType;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Integer totalQuestions;
    private Integer totalTime;
    private LessonSessionStatus status;
    private LocalDateTime completedAt;
}
