package com.glotrush.services.lesson;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.mapping.LessonEntityToLessonResponse;
import com.glotrush.services.progress.IProgressService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.glotrush.builder.LessonBuilder;
import com.glotrush.dto.request.CompleteLessonRequest;
import com.glotrush.dto.response.CompleteLessonResponse;
import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.UserLessonProgressSummary;
import com.glotrush.dto.response.UserProgressResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.UserLessonProgress;
import com.glotrush.entities.UserProgress;
import com.glotrush.enumerations.LessonStatus;
import com.glotrush.exceptions.LessonNotFoundException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.mapping.LessonRequestToLessonEntity;
import com.glotrush.repositories.*;
import com.glotrush.entities.Topic;
import com.glotrush.exceptions.TopicNotFoundException;

import com.glotrush.utils.LevelUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonService implements ILessonService {
    private final MessageSource messageSource;
    private final LessonRepository lessonRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;
    private final AccountsRepository accountsRepository;
    private final IProgressService progressService;
    private final LessonBuilder lessonBuilder;
    private final TopicRepository topicRepository;
    private final LessonEntityToLessonResponse lessonEntityToLessonResponse;
    private final LessonRequestToLessonEntity lessonRequestToLessonEntity;

    protected final Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }


    @Override
    public List<LessonResponse> getLessonsByTopic(UUID topicId, UUID accountId) {
      return lessonRepository.findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topicId).stream().map(lesson -> mapToLessonResponse(lesson, accountId)).toList();
    }

    @Override
    public LessonResponse getLessonById(UUID lessonId, UUID accountId) {
       Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, getCurrentLocale())));
        return mapToLessonResponse(lesson, accountId);
    }

    @Override
    public UserLessonProgressSummary startLesson(UUID accountId, UUID lessonId) {
       
        Accounts account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, getCurrentLocale())));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, getCurrentLocale())));
                
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
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, getCurrentLocale())));

        UserLessonProgress progress = userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId)
                .orElseThrow(() -> new LessonNotFoundException(messageSource.getMessage("error.lesson.progress.notfound", null, getCurrentLocale())));

        boolean isSuccessful = lessonRequest.getScore() >= lesson.getPassScorePercentage();
        progress.setTotalAttempts(progress.getTotalAttempts() + 1);
        progress.setTimeSpentSeconds(progress.getTimeSpentSeconds() + lessonRequest.getTimeSpentSeconds());
        progress.setLastAttemptAt(LocalDateTime.now());

        if (!isSuccessful) {
            progress.setFailedAttempts(progress.getFailedAttempts() + 1);
            userLessonProgressRepository.save(progress);
            return CompleteLessonResponse.builder()
                    .success(false)
                    .message(messageSource.getMessage("error.lesson.failed", null, getCurrentLocale()))
                    .xpEarned(0)
                    .totalXP(0L)
                    .build();
        }

        boolean isFirstCompletion = progress.getStatus() != LessonStatus.COMPLETED;
        progress.setStatus(LessonStatus.COMPLETED);

        // On garde le meilleur score
        if (progress.getScore() == null || lessonRequest.getScore() > progress.getScore()) {
            progress.setScore(lessonRequest.getScore());
        }

        userLessonProgressRepository.save(progress);

        if (isFirstCompletion) {
            return handleFirstCompletion(accountId, lesson);
        } else {
            return handleRecompletion(accountId, lesson);
        }
    }

    private CompleteLessonResponse handleFirstCompletion(UUID accountId, Lesson lesson) {
        Integer xpEarned = lesson.getXpReward();
        
        UserProgress topicProgress = progressService.getOrCreateProgress(accountId, lesson.getTopic().getId());
        Integer oldLevel = LevelUtils.calculateLevel(topicProgress.getTotalXP());

        topicProgress = progressService.addXP(accountId, lesson.getTopic().getId(), xpEarned);
        topicProgress = progressService.incrementLessonCompletion(accountId, lesson.getTopic().getId());
        topicProgress = progressService.updateLastStudiedAt(accountId, lesson.getTopic().getId());
        Integer newLevel = LevelUtils.calculateLevel(topicProgress.getTotalXP());
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
        // TODO fix content plus tard
        return lessonBuilder.mapLessonToLessonResponse(lesson, progress, "");
    }

    @Override
    public void removeLesson(UUID lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, getCurrentLocale()));
        }
        lessonRepository.deleteById(lessonId);
    }

    @Override
    public LessonResponse updateLesson(UUID lessonId, LessonRequest lessonRequest) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, getCurrentLocale())));

        if (lessonRequest.getTopicId() != null && !lessonRequest.getTopicId().equals(lesson.getTopic().getId())) {
            Topic topic = topicRepository.findById(lessonRequest.getTopicId())
                    .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, getCurrentLocale())));
            lesson.setTopic(topic);
        }

        lessonRequestToLessonEntity.updateLessonFromRequest(lessonRequest, lesson, messageSource);

        lessonRepository.save(lesson);
        return lessonEntityToLessonResponse.lessonEntityToLessonResponse(lesson, messageSource);
    }

    @Override
    public LessonResponse createLesson(LessonRequest lessonRequest){
        Topic topic = topicRepository.findById(lessonRequest.getTopicId())
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, getCurrentLocale())));

        Lesson lesson = lessonRequestToLessonEntity.lessonRequestToLessonEntity(lessonRequest, messageSource);
        lesson.setTopic(topic);

        lessonRepository.save(lesson);
        return lessonEntityToLessonResponse.lessonEntityToLessonResponse(lesson, messageSource);
    }

}
