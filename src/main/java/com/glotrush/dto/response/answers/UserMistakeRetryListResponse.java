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
public class UserMistakeRetryListResponse {
    private UUID userMistakeId;
    private LessonType lessonType;
    private LearningType learningType;
    private String topicName;
    private String nextReviewIndication;
    private Integer nbCorrectResponses;
    private String answeredQuestion;
    private String userAnswer;
    private String correctAnswer;
    private List<String> options;
    private LocalDateTime createdAt;
}
