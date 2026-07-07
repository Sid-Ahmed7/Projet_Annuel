package com.glotrush.services.ai;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.enumerations.LessonType;
import java.util.UUID;

public interface IAILessonGeneratorService {

    LessonRequest generateLesson(UUID accountId, UUID topicId, LessonType lessonType, String description, Integer itemCount);

    LessonRequest generateChallengeContent(UUID accountId, UUID sourceLanguageId, UUID targetLanguageId, LessonType lessonType, String description, Integer itemCount);

    LessonRequest modifyLesson(UUID accountId, UUID lessonId, String prompt, Integer itemCount, LessonRequest currentLessonRequest);
}
