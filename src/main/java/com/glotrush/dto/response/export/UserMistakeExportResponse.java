package com.glotrush.dto.response.export;

import java.time.LocalDateTime;
import java.util.UUID;

import com.glotrush.enumerations.LessonType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMistakeExportResponse {
    private UUID questionId;
    private LessonType lessonType;
    private LocalDateTime nextReviewAt;
    private String userAnswer;
    private Boolean isResolved;
}
