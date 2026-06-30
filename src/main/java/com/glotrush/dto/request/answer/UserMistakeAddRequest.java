package com.glotrush.dto.request.answer;

import com.glotrush.enumerations.LearningType;
import com.glotrush.enumerations.LessonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMistakeAddRequest {
    private UUID questionId;
    private LessonType lessonType;
    private UUID topicId;
    private LearningType learningType;
    private String userAnswer;
}
