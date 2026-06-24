package com.glotrush.dto.request.answer;

import com.glotrush.enumerations.LearningType;
import com.glotrush.enumerations.LessonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMistakeAddMultipleRequest {
    private List<UUID> questionIds;
    private LessonType lessonType;
    private UUID topicId;
    private LearningType learningType;
}
