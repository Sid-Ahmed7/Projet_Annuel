package com.glotrush.services.lesson;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.request.CompleteLessonRequest;
import com.glotrush.dto.response.CompleteLessonResponse;
import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.UserLessonProgressSummary;

public interface ILessonService {
    
    List<LessonResponse> getLessonsByTopic(UUID topicId, UUID accountId);
    LessonResponse getLessonById(UUID lessonId, UUID accountId);
    UserLessonProgressSummary startLesson(UUID accountId, UUID lessonId);
    CompleteLessonResponse completeLesson(UUID accountId, UUID lessonId, CompleteLessonRequest lessonRequest);
     
}
