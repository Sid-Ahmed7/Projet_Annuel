package com.glotrush.services.progress;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.glotrush.dto.response.LanguageLevelResponse;
import com.glotrush.dto.response.LastLessonResponse;
import com.glotrush.dto.response.ProgressOverviewResponse;
import com.glotrush.dto.response.UserProgressResponse;
import com.glotrush.entities.UserProgress;

public interface IProgressService {
    
    ProgressOverviewResponse getProgressOverview(UUID accountId);
    UserProgressResponse getProgressByTopic(UUID accountId, UUID topicId);
    List<UserProgressResponse> getProgressByLanguage(UUID accountId, UUID languageId);
    UserProgress getOrCreateProgress(UUID accountId, UUID topicId);
    UserProgress addXP(UUID accountId, UUID topicId, Integer xpToAdd);
    UserProgress incrementLessonCompletion(UUID accountId, UUID topicId);
    UserProgress updateAnswerStats(UUID accountId, UUID topicId, Integer correct, Integer total);
    UserProgress updateLastStudiedAt(UUID accountId, UUID topicId);
    List<LanguageLevelResponse> getLanguageLevel(UUID accountId);
    Optional<LastLessonResponse> getLastStudiedLesson(UUID accountId);
    
}
