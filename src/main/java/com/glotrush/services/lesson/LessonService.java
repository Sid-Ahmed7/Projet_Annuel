package com.glotrush.services.lesson;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.glotrush.builder.LessonBuilder;
import com.glotrush.dto.request.CompleteLessonRequest;
import com.glotrush.dto.response.CompleteLessonResponse;
import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.UserLessonProgressSummary;
import com.glotrush.dto.response.UserProgressResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.LessonContent;
import com.glotrush.entities.UserLessonProgress;
import com.glotrush.entities.UserProgress;
import com.glotrush.enumerations.LessonStatus;
import com.glotrush.exceptions.LessonNotFoundException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.LessonContentRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.UserLessonProgressRepository;
import com.glotrush.services.progress.ProgressService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonService implements ILessonService {
    private final LessonRepository lessonRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;
    private final LessonContentRepository lessonContentRepository;
    private final AccountsRepository accountsRepository;
    private final ProgressService progressService;
    private final LessonBuilder lessonBuilder;


    @Override
    public List<LessonResponse> getLessonsByTopic(UUID topicId, UUID accountId) {
      return lessonRepository.findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topicId).stream().map(lesson -> mapToLessonResponse(lesson, accountId)).toList();
    }

    @Override
    public LessonResponse getLessonById(UUID lessonId, UUID accountId) {
       Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found"));
        return mapToLessonResponse(lesson, accountId);
    }

    @Override
    public UserLessonProgressSummary startLesson(UUID accountId, UUID lessonId) {
       
        Accounts account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new UserNotFoundException("Account not found"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found"));    
                
        UserLessonProgress progress = userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId)
                .orElseGet(() -> lessonBuilder.createNewLessonProgress(account, lesson));

        if (progress.getStatus() == LessonStatus.NOT_STARTED) {
            progress.setStatus(LessonStatus.IN_PROGRESS);
        }

        progress.setLastAttemptAt(LocalDateTime.now());
        progress = userLessonProgressRepository.save(progress);
        return lessonBuilder.mapToUserLessonProgressSummary(progress);
    }

    @Override
    public CompleteLessonResponse completeLesson(UUID accountId, UUID lessonId, CompleteLessonRequest lessonRequest) {
       Lesson lesson  = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found"));

        UserLessonProgress progress = userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson progress not found"));

        boolean isLessonCompleted = progress.getStatus() == LessonStatus.COMPLETED;
    
        progress.setStatus(LessonStatus.COMPLETED);
        progress.setScore(lessonRequest.getScore());
        progress.setAttempts(progress.getAttempts() + 1);
        progress.setTimeSpentSeconds(progress.getTimeSpentSeconds() + lessonRequest.getTimeSpentSeconds());
        progress.setCompletedAt(LocalDateTime.now());
        progress.setLastAttemptAt(LocalDateTime.now());

        userLessonProgressRepository.save(progress);

        if (!isLessonCompleted) {
            return handleFirstCompletion(accountId, lesson);
        } else {
            return handleRecompletion(accountId, lesson);
        }
    }

      private CompleteLessonResponse handleFirstCompletion(UUID accountId, Lesson lesson) {
        Integer xpEarned = lesson.getXpReward();
        
        UserProgress topicProgress = progressService.getOrCreateProgress(accountId, lesson.getTopic().getId());
        Integer oldLevel = topicProgress.getLevel();

        topicProgress = progressService.addXP(accountId, lesson.getTopic().getId(), xpEarned);
        topicProgress = progressService.incrementLessonCompletion(accountId, lesson.getTopic().getId());

        Integer newLevel = topicProgress.getLevel();
        boolean leveledUp = !oldLevel.equals(newLevel);

        UserProgressResponse progressResponse = progressService.getProgressByTopic(accountId, lesson.getTopic().getId());

        return lessonBuilder.buildCompleteLessonResponse(leveledUp, xpEarned, topicProgress, progressResponse, newLevel);
    }

    private CompleteLessonResponse handleRecompletion(UUID accountId, Lesson lesson) {
        UserProgressResponse progressResponse = progressService.getProgressByTopic(accountId, lesson.getTopic().getId());
        return lessonBuilder.buildRecompletedLessonResponse(progressResponse);
    }

    private LessonResponse mapToLessonResponse(Lesson lesson, UUID accountId) {
        Optional<UserLessonProgress> progress = accountId != null ? userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lesson.getId()) : Optional.empty();

        String content = lessonContentRepository.findByLesson_Id(lesson.getId())
                .map(LessonContent::getContent)
                .orElse(null);

        return lessonBuilder.mapToLessonResponse(lesson, progress, content);
    }
    
}
