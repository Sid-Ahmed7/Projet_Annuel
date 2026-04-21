package com.glotrush.services.lesson;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.glotrush.dto.request.LessonReorderRequest;
import com.glotrush.dto.request.LessonRequest;
import com.glotrush.mapping.LessonEntityToLessonResponse;
import com.glotrush.services.progress.IProgressService;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.glotrush.builder.LessonBuilder;
import com.glotrush.dto.request.CompleteLessonRequest;
import com.glotrush.dto.response.CompleteLessonResponse;
import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.LessonSummaryResponse;
import com.glotrush.dto.response.TopicLessonsResponse;
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
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.UserLessonProgressRepository;
import com.glotrush.entities.Topic;
import com.glotrush.exceptions.TopicNotFoundException;

import com.glotrush.utils.LevelUtils;
import com.glotrush.utils.LocaleUtils;
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

    @Override
    public List<LessonSummaryResponse> getLessonsByTopic(UUID topicId, UUID accountId) {
        return lessonRepository.findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topicId).stream()
                .map(lesson -> {
                    boolean isAlreadyFinish = userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lesson.getId())
                            .map(progress -> progress.getTotalAttempts() > 0)
                            .orElse(false);
                    return lessonEntityToLessonResponse.lessonToLessonSummaryResponse(lesson, isAlreadyFinish);
                })
                .toList();
    }

    @Override
    public TopicLessonsResponse getTopicLessonsDetails(UUID topicId, UUID accountId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale())));

        List<LessonSummaryResponse> lessons = getLessonsByTopic(topicId, accountId);

        UserProgress userProgress = progressService.getOrCreateProgress(accountId, topicId);

        return TopicLessonsResponse.builder()
                .topicTitle(topic.getName())
                .lessons(lessons)
                .examPassed(userProgress.getExamPassed())
                .examAttempts(userProgress.getExamAttempts())
                .lastAccuracy(userProgress.getBestExamScore() != null ? userProgress.getBestExamScore() / 100.0 : null)
                .build();
    }

    @Override
    public LessonResponse getLessonById(UUID lessonId, UUID accountId) {
       Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, LocaleUtils.getCurrentLocale())));
        return lessonEntityToLessonResponse.lessonEntityToLessonResponse(lesson, messageSource);
    }

    @Override
    public UserLessonProgressSummary startLesson(UUID accountId, UUID lessonId) {
        Accounts account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, LocaleUtils.getCurrentLocale())));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, LocaleUtils.getCurrentLocale())));

        UserLessonProgress progress;
        try {
            progress = userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId)
                    .orElseGet(() -> userLessonProgressRepository.saveAndFlush(lessonBuilder.createNewLessonProgress(account, lesson)));
        } catch (DataIntegrityViolationException e) {
            // En cas de concurrence (ex: React StrictMode), on retente de récupérer l'objet créé par l'autre thread
            progress = userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId)
                    .orElseThrow(() -> e); // Si toujours rien, on relance l'erreur d'origine
        }

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
                .orElseThrow(() -> new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, LocaleUtils.getCurrentLocale())));

        UserLessonProgress progress = userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId)
                .orElseThrow(() -> new LessonNotFoundException(messageSource.getMessage("error.lesson.progress.notfound", null, LocaleUtils.getCurrentLocale())));

        progress.setTotalAttempts(progress.getTotalAttempts() + 1);
        progress.setTimeSpentSeconds(progress.getTimeSpentSeconds() + lessonRequest.getTimeSpentSeconds());
        progress.setLastAttemptAt(LocalDateTime.now());
        if (lessonRequest.getFeedback() != null) {
            progress.setUserFeedback(lessonRequest.getFeedback());
        }

        // Mise à jour des stats de réponses globales
        if (lessonRequest.getCorrectAnswers() != null && lessonRequest.getTotalAnswers() != null) {
            progressService.updateAnswerStats(accountId, lesson.getTopic().getId(), lessonRequest.getCorrectAnswers(), lessonRequest.getTotalAnswers());

            // Check if score is high enough to complete
            double score = (double) lessonRequest.getCorrectAnswers() / lessonRequest.getTotalAnswers() * 100;
            if (lesson.getMinScoreRequired() != null && score < lesson.getMinScoreRequired()) {
                userLessonProgressRepository.save(progress);
                UserProgress topicProgress = progressService.getOrCreateProgress(accountId, lesson.getTopic().getId());
                UserProgressResponse progressResponse = progressService.getProgressByTopic(accountId, lesson.getTopic().getId());
                Integer currentLevel = LevelUtils.calculateLevel(topicProgress.getTotalXP());
                
                return CompleteLessonResponse.builder()
                        .success(false)
                        .message(messageSource.getMessage("error.lesson.failed", null, LocaleUtils.getCurrentLocale()))
                        .xpEarned(0)
                        .totalXP(topicProgress.getTotalXP())
                        .currentLevel(currentLevel)
                        .leveledUp(false)
                        .progress(progressResponse)
                        .build();
            }
        }

        boolean isFirstCompletion = progress.getStatus() != LessonStatus.COMPLETED;
        progress.setStatus(LessonStatus.COMPLETED);

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

        return lessonBuilder.buildCompleteLessonResponse(leveledUp, xpEarned, topicProgress, progressResponse);
    }

    private CompleteLessonResponse handleRecompletion(UUID accountId, Lesson lesson) {
        UserProgress topicProgress = progressService.getOrCreateProgress(accountId, lesson.getTopic().getId());
        UserProgressResponse progressResponse = progressService.getProgressByTopic(accountId, lesson.getTopic().getId());
        return lessonBuilder.buildCompleteLessonResponse(false, 0, topicProgress, progressResponse);
    }

    @Override
    public void removeLesson(UUID lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, LocaleUtils.getCurrentLocale()));
        }
        lessonRepository.deleteById(lessonId);
    }

    @Override
    public LessonResponse updateLesson(UUID lessonId, LessonRequest lessonRequest) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, LocaleUtils.getCurrentLocale())));

        if (lessonRequest.getTopicId() != null && !lessonRequest.getTopicId().equals(lesson.getTopic().getId())) {
            Topic topic = topicRepository.findById(lessonRequest.getTopicId())
                    .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale())));
            lesson.setTopic(topic);
        }

        lessonRequestToLessonEntity.updateLessonFromRequest(lessonRequest, lesson, messageSource);

        lessonRepository.save(lesson);
        return lessonEntityToLessonResponse.lessonEntityToLessonResponse(lesson, messageSource);
    }

    @Override
    public LessonResponse createLesson(LessonRequest lessonRequest){
        Topic topic = topicRepository.findById(lessonRequest.getTopicId())
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale())));

        Integer maxOrderIndex = lessonRepository.findMaxOrderIndexByTopicId(lessonRequest.getTopicId());

        Lesson lesson = lessonRequestToLessonEntity.lessonRequestToLessonEntity(lessonRequest, messageSource);
        lesson.setTopic(topic);
        lesson.setOrderIndex(maxOrderIndex + 1);

        lessonRepository.save(lesson);
        return lessonEntityToLessonResponse.lessonEntityToLessonResponse(lesson, messageSource);
    }

    @Override
    public LessonResponse toggleLessonStatus(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, LocaleUtils.getCurrentLocale())));

        lesson.setIsActive(!lesson.getIsActive());
        
        lessonRepository.save(lesson);
        return lessonEntityToLessonResponse.lessonEntityToLessonResponse(lesson, messageSource);
    }

    @Override
    public List<LessonSummaryResponse> getLessonsByTopicForAdmin(UUID topicId, UUID accountId) {
        return lessonRepository.findByTopic_IdOrderByOrderIndexAsc(topicId).stream()
                .map(lesson -> {
                    boolean isAlreadyFinish = userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lesson.getId())
                            .map(progress -> progress.getTotalAttempts() > 0)
                            .orElse(false);
                    return lessonEntityToLessonResponse.lessonToLessonSummaryResponse(lesson, isAlreadyFinish);
                })
                .toList();
    }

    @Override
    public void reorderLessons(UUID topicId, List<LessonReorderRequest> requests) {
        List<Lesson> allLessons = lessonRepository.findByTopic_Id(topicId);
        Map<UUID, Lesson> lessonMap = allLessons.stream()
                .collect(Collectors.toMap(Lesson::getId, lesson -> lesson));

        for (LessonReorderRequest request : requests) {
            Lesson lesson = lessonMap.get(request.id());
            if (lesson != null) {
                lesson.setOrderIndex(request.orderIndex());
            }
        }

        allLessons.sort(Comparator.comparing(Lesson::getOrderIndex)
                .thenComparing(Lesson::getId));

        for (int index = 0; index < allLessons.size(); index++) {
            allLessons.get(index).setOrderIndex(index);
        }

        lessonRepository.saveAll(allLessons);
    }

}
