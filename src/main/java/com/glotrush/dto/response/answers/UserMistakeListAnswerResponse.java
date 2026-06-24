package com.glotrush.dto.response.answers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.glotrush.enumerations.LearningType;
import com.glotrush.enumerations.LessonType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMistakeListAnswerResponse {
    private UUID mistakeItemId;
    private LessonType lessonType;
    private LearningType learningType;
    private String topicName;
    private LocalDateTime addedAt;
    private String nextReviewIn;
    private Integer consecutiveCorrect;
    private String userWrongAnswer;
    private String questionLabel;
    private String correctAnswer;
    private List<String> options;
}
