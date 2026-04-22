package com.glotrush.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import com.glotrush.config.TestMessageSourceConfig;
import com.glotrush.dispatcher.notifications.NotificationDispatcher;
import com.glotrush.entities.lesson.FlashcardLesson;
import com.glotrush.mapping.LessonEntityToLessonResponse;
import com.glotrush.mapping.LessonRequestToLessonEntity;
import com.glotrush.repositories.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.glotrush.builder.LessonBuilder;
import com.glotrush.dto.request.CompleteLessonRequest;
import com.glotrush.dto.request.lesson.FlashcardLessonRequest;
import com.glotrush.dto.request.lesson.MatchingPairLessonRequest;
import com.glotrush.dto.request.lesson.QcmLessonRequest;
import com.glotrush.dto.request.lesson.SortingExerciseLessonRequest;
import com.glotrush.dto.response.lesson.FlashcardLessonResponse;
import com.glotrush.dto.response.lesson.MatchingPairLessonResponse;
import com.glotrush.dto.response.lesson.QcmLessonResponse;
import com.glotrush.dto.response.lesson.SortingExerciseLessonResponse;
import com.glotrush.factory.LessonTestFactory;
import com.glotrush.exceptions.TopicNotFoundException;
import com.glotrush.dto.response.CompleteLessonResponse;
import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.LessonSummaryResponse;
import com.glotrush.dto.response.TopicLessonsResponse;
import com.glotrush.dto.response.UserLessonProgressSummary;
import com.glotrush.dto.response.UserProgressResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Topic;
import com.glotrush.entities.UserLessonProgress;
import com.glotrush.entities.UserProgress;
import com.glotrush.enumerations.LessonStatus;
import com.glotrush.exceptions.LessonNotFoundException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.entities.lesson.MatchingPairLesson;
import com.glotrush.entities.lesson.QcmLesson;
import com.glotrush.entities.lesson.SortingExerciseLesson;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.UserLessonProgressRepository;
import com.glotrush.services.lesson.LessonService;
import com.glotrush.services.progress.ProgressService;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(classes = TestMessageSourceConfig.class)
@DisplayName("LessonService Unit Tests")
class LessonServiceTest {
    @Mock
    private MessageSource messageSource;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserLessonProgressRepository userLessonProgressRepository;

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private ProgressService progressService;

    @Mock
    private LessonEntityToLessonResponse lessonEntityToLessonResponse;
    @Mock
    private LessonRequestToLessonEntity lessonRequestToLessonEntity;

    @Mock
    private LessonBuilder lessonBuilder;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    private LessonService lessonService;

    private UUID accountId;
    private UUID lessonId;
    private UUID topicId;
    private Accounts account;
    private FlashcardLesson lesson;
    private UserLessonProgress userLessonProgress;

    @BeforeEach
    void setUp() {
        lessonService = new LessonService(messageSource, lessonRepository, userLessonProgressRepository, accountsRepository, progressService, lessonBuilder, topicRepository, lessonEntityToLessonResponse, lessonRequestToLessonEntity, notificationDispatcher);
        accountId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
        topicId = UUID.randomUUID();

        account = Accounts.builder()
                .id(accountId)
                .email("test@exemple.com")
                .build();

        Topic topic = Topic.builder()
                .id(topicId)
                .name("Spring Basics")
                .build();

        lesson = FlashcardLesson.builder()
                .id(lessonId)
                .title("Introduction to Spring")
                .description("Learn about Spring framework basics")
                .topic(topic)
                .xpReward(50)
                .orderIndex(1)
                .isActive(true)
                .minLevelRequired(1)
                .minScoreRequired(70)
                .durationMinutes(15)
                .build();

        userLessonProgress = UserLessonProgress.builder()
          .id(UUID.randomUUID())
                .account(account)
                .lesson(lesson)
                .status(LessonStatus.NOT_STARTED)
                .totalAttempts(0)
                .timeSpentSeconds(0)
                .build();
    }


    @Test
    @DisplayName("Should return all active lessons for topic")
    void shouldGetLessonsByTopic() {
        LessonSummaryResponse expectedResponse = LessonSummaryResponse.builder()
                .id(lessonId)
                .title("Introduction to Spring")
                .isAlreadyFinish(false)
                .build();

        when(lessonRepository.findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topicId))
                .thenReturn(List.of(lesson));
        when(userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId))
                .thenReturn(Optional.empty());
        when(lessonEntityToLessonResponse.lessonToLessonSummaryResponse(eq(lesson), eq(false)))
                .thenReturn(expectedResponse);

        List<LessonSummaryResponse> result = lessonService.getLessonsByTopic(topicId, accountId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Introduction to Spring");
        assertThat(result.get(0).getIsAlreadyFinish()).isFalse();

        verify(lessonRepository).findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topicId);
    }

    @Test
    @DisplayName("Should return lessons with isAlreadyFinish true when attempts > 0")
    void shouldGetLessonsByTopicWithAlreadyFinished() {
        userLessonProgress.setTotalAttempts(1);
        LessonSummaryResponse expectedResponse = LessonSummaryResponse.builder()
                .id(lessonId)
                .title("Introduction to Spring")
                .isAlreadyFinish(true)
                .build();

        when(lessonRepository.findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topicId))
                .thenReturn(List.of(lesson));
        when(userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId))
                .thenReturn(Optional.of(userLessonProgress));
        when(lessonEntityToLessonResponse.lessonToLessonSummaryResponse(eq(lesson), eq(true)))
                .thenReturn(expectedResponse);

        List<LessonSummaryResponse> result = lessonService.getLessonsByTopic(topicId, accountId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsAlreadyFinish()).isTrue();
    }

    @Test
    @DisplayName("Should return empty list when no lessons for topic")
    void shouldReturnEmptyListWhenNoLessonsForTopic() {
        when(lessonRepository.findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topicId))
                .thenReturn(Collections.emptyList());

        List<LessonSummaryResponse> result = lessonService.getLessonsByTopic(topicId, accountId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return topic lessons details")
    void shouldGetTopicLessonsDetails() {
        Topic topic = Topic.builder()
                .id(topicId)
                .name("Spring Basics")
                .build();

        LessonSummaryResponse lessonSummary = LessonSummaryResponse.builder()
                .id(lessonId)
                .title("Introduction to Spring")
                .isAlreadyFinish(true)
                .build();

        UserProgress progress = UserProgress.builder()
                .examPassed(true)
                .examAttempts(3)
                .bestExamScore(85.0)
                .build();

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(lessonRepository.findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topicId)).thenReturn(List.of(lesson));
        when(userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId))
                .thenReturn(Optional.of(UserLessonProgress.builder().totalAttempts(1).build()));
        when(lessonEntityToLessonResponse.lessonToLessonSummaryResponse(eq(lesson), eq(true)))
                .thenReturn(lessonSummary);
        when(progressService.getOrCreateProgress(accountId, topicId)).thenReturn(progress);

        TopicLessonsResponse result = lessonService.getTopicLessonsDetails(topicId, accountId);

        assertThat(result).isNotNull();
        assertThat(result.getTopicTitle()).isEqualTo("Spring Basics");
        assertThat(result.getLessons()).hasSize(1);
        assertThat(result.getExamPassed()).isTrue();
        assertThat(result.getExamAttempts()).isEqualTo(3);
        assertThat(result.getLastAccuracy()).isEqualTo(0.85);
    }

    @Test
    @DisplayName("Should throw exception when topic not found on topic lessons details")
    void shouldThrowExceptionWhenTopicNotFoundOnTopicLessonsDetails() {
        when(topicRepository.findById(topicId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("error.topic.notfound"), isNull(), any(Locale.class))).thenReturn("Topic not found");

        assertThatThrownBy(() -> lessonService.getTopicLessonsDetails(topicId, accountId))
                .isInstanceOf(TopicNotFoundException.class)
                .hasMessage("Topic not found");

        verify(topicRepository).findById(topicId);
    }

    @Test
    @DisplayName("Should return lesson by id")
    void shouldGetLessonById() {
        LessonResponse expectedResponse = LessonResponse.builder()
                .id(lessonId)
                .title("Introduction to Spring")
                .description("")
                .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonEntityToLessonResponse.lessonEntityToLessonResponse(eq(lesson), any()))
                .thenReturn(expectedResponse);

        LessonResponse result = lessonService.getLessonById(lessonId, accountId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(lessonId);

        verify(lessonRepository).findById(lessonId);
    }

    @Test
    @DisplayName("Should throw exception when lesson not found")
    void shouldThrowExceptionWhenLessonNotFound() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("error.lesson.notfound"), isNull(), any(Locale.class))).thenReturn("Lesson not found");

        assertThatThrownBy(() -> lessonService.getLessonById(lessonId, accountId))
                .isInstanceOf(LessonNotFoundException.class)
                .hasMessage("Lesson not found");

        verify(lessonRepository).findById(lessonId);
    }

    @Test
    @DisplayName("Should start lesson and set status to IN_PROGRESS")
    void shouldStartLessonSuccessfully() {
        UserLessonProgressSummary expectedSummary = UserLessonProgressSummary.builder()
                .status(LessonStatus.IN_PROGRESS)
                .totalAttempts(0)
                .build();

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId))
                .thenReturn(Optional.empty());
        when(lessonBuilder.createNewLessonProgress(account, lesson))
                .thenReturn(userLessonProgress);
        when(userLessonProgressRepository.save(any(UserLessonProgress.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(lessonBuilder.mapToUserLessonProgressSummary(any())).thenReturn(expectedSummary);

        UserLessonProgressSummary result = lessonService.startLesson(accountId, lessonId);

        assertThat(result.getStatus()).isEqualTo(LessonStatus.IN_PROGRESS);

        verify(userLessonProgressRepository).save(any(UserLessonProgress.class));
    }

    @Test
    @DisplayName("Should update lastAttemptAt when resuming lesson")
    void shouldUpdateLastAttemptAtWhenResumingLesson() {
        userLessonProgress.setStatus(LessonStatus.IN_PROGRESS);

        UserLessonProgressSummary expectedSummary = UserLessonProgressSummary.builder()
                .status(LessonStatus.IN_PROGRESS)
                .build();

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId))
                .thenReturn(Optional.of(userLessonProgress));
        when(userLessonProgressRepository.save(any(UserLessonProgress.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(lessonBuilder.mapToUserLessonProgressSummary(any())).thenReturn(expectedSummary);

        UserLessonProgressSummary result = lessonService.startLesson(accountId, lessonId);

        assertThat(result.getStatus()).isEqualTo(LessonStatus.IN_PROGRESS);

        verify(lessonBuilder, never()).createNewLessonProgress(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when account not found on start lesson")
    void shouldThrowExceptionWhenAccountNotFoundOnStartLesson() {
        when(accountsRepository.findById(accountId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("error.auth.account_not_found"), isNull(), any(Locale.class))).thenReturn("Account not found");

        assertThatThrownBy(() -> lessonService.startLesson(accountId, lessonId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Account not found");

        verify(accountsRepository).findById(accountId);
    }

    @Test
    @DisplayName("Should throw exception when lesson not found on start lesson")
    void shouldThrowExceptionWhenLessonNotFoundOnStartLesson() {
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("error.lesson.notfound"), isNull(), any(Locale.class))).thenReturn("Lesson not found");

        assertThatThrownBy(() -> lessonService.startLesson(accountId, lessonId))
                .isInstanceOf(LessonNotFoundException.class)
                .hasMessage("Lesson not found");
    }

    @Test
    @DisplayName("Should complete lesson first time and award XP")
    void shouldCompleteLessonFirstTimeAndAwardXP() {
        CompleteLessonRequest request = new CompleteLessonRequest();
        request.setTimeSpentSeconds(600);

        userLessonProgress.setStatus(LessonStatus.IN_PROGRESS);

        UserProgress topicProgress = UserProgress.builder()
                .totalXP(50L)
                .build();

        UserProgressResponse progressResponse = new UserProgressResponse();

        CompleteLessonResponse expectedResponse = CompleteLessonResponse.builder()
                .success(true)
                .xpEarned(50)
                .leveledUp(false)
                .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId))
                .thenReturn(Optional.of(userLessonProgress));
        when(progressService.getOrCreateProgress(accountId, topicId)).thenReturn(topicProgress);
        when(progressService.addXP(accountId, topicId, 50)).thenReturn(topicProgress);
        when(progressService.incrementLessonCompletion(accountId, topicId)).thenReturn(topicProgress);
        when(progressService.updateLastStudiedAt(accountId, topicId)).thenReturn(topicProgress);
        when(progressService.getProgressByTopic(accountId, topicId)).thenReturn(progressResponse);
        when(lessonBuilder.buildCompleteLessonResponse(eq(false), eq(50), any(), any()))
                .thenReturn(expectedResponse);

        CompleteLessonResponse result = lessonService.completeLesson(accountId, lessonId, request);

        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getXpEarned()).isEqualTo(50);
        assertThat(userLessonProgress.getStatus()).isEqualTo(LessonStatus.COMPLETED);
        assertThat(userLessonProgress.getTotalAttempts()).isEqualTo(1);

        verify(userLessonProgressRepository).save(any(UserLessonProgress.class));
        verify(progressService).addXP(accountId, topicId, 50);
        verify(progressService).updateLastStudiedAt(accountId, topicId);
        verify(progressService).incrementLessonCompletion(accountId, topicId);
    }

    @Test
    @DisplayName("Should fail to complete lesson when score is too low")
    void shouldFailToCompleteLessonWhenScoreIsLow() {
        CompleteLessonRequest request = new CompleteLessonRequest();
        request.setTimeSpentSeconds(600);
        request.setCorrectAnswers(5);
        request.setTotalAnswers(10); // 50% score, below default 70%

        userLessonProgress.setStatus(LessonStatus.IN_PROGRESS);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId))
                .thenReturn(Optional.of(userLessonProgress));
        when(messageSource.getMessage(eq("error.lesson.failed"), isNull(), any(Locale.class))).thenReturn("Lesson failed");
        when(progressService.getOrCreateProgress(accountId, topicId)).thenReturn(UserProgress.builder().totalXP(0L).build());
        when(progressService.getProgressByTopic(accountId, topicId)).thenReturn(new UserProgressResponse());

        CompleteLessonResponse result = lessonService.completeLesson(accountId, lessonId, request);

        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Lesson failed");
        assertThat(result.getXpEarned()).isEqualTo(0);
        assertThat(userLessonProgress.getStatus()).isEqualTo(LessonStatus.IN_PROGRESS);
        assertThat(userLessonProgress.getTotalAttempts()).isEqualTo(1);

        verify(userLessonProgressRepository).save(any(UserLessonProgress.class));
        verify(progressService).updateAnswerStats(accountId, topicId, 5, 10);
        verify(progressService, never()).addXP(any(), any(), any());
    }

    @Test
    @DisplayName("Should complete lesson with level up")
    void shouldCompleteLessonWithLevelUp() {
        CompleteLessonRequest request = new CompleteLessonRequest();
        request.setTimeSpentSeconds(600);

        userLessonProgress.setStatus(LessonStatus.IN_PROGRESS);

        UserProgress topicProgressBefore = UserProgress.builder()
                .totalXP(990L)
                .build();

        UserProgress topicProgressAfter = UserProgress.builder()
                .totalXP(1040L)
                .build();

        UserProgressResponse progressResponse = new UserProgressResponse();

        CompleteLessonResponse expectedResponse = CompleteLessonResponse.builder()
                .success(true)
                .xpEarned(50)
                .leveledUp(true)
                .newLevel(5)
                .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId))
                .thenReturn(Optional.of(userLessonProgress));
        when(progressService.getOrCreateProgress(accountId, topicId)).thenReturn(topicProgressBefore);
        when(progressService.updateLastStudiedAt(accountId, topicId)).thenReturn(topicProgressAfter);  
        when(progressService.addXP(accountId, topicId, 50)).thenReturn(topicProgressAfter);
        when(progressService.incrementLessonCompletion(accountId, topicId)).thenReturn(topicProgressAfter);
        when(progressService.getProgressByTopic(accountId, topicId)).thenReturn(progressResponse);
        when(lessonBuilder.buildCompleteLessonResponse(eq(true), eq(50), any(), any()))
                .thenReturn(expectedResponse);

        CompleteLessonResponse result = lessonService.completeLesson(accountId, lessonId, request);

        verify(progressService).updateLastStudiedAt(accountId, topicId);
        assertThat(result.getLeveledUp()).isTrue();
        assertThat(result.getNewLevel()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should handle lesson recompletion without XP")
    void shouldHandleLessonRecompletionWithoutXP() {
        CompleteLessonRequest request = new CompleteLessonRequest();
        request.setTimeSpentSeconds(500);

        userLessonProgress.setStatus(LessonStatus.COMPLETED);
        userLessonProgress.setTotalAttempts(1);

        UserProgressResponse progressResponse = new UserProgressResponse();
        UserProgress topicProgress = new UserProgress();

        CompleteLessonResponse expectedResponse = CompleteLessonResponse.builder()
                .success(true)
                .xpEarned(0)
                .message("Lesson already completed. No XP awarded.")
                .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId))
                .thenReturn(Optional.of(userLessonProgress));
        when(progressService.getOrCreateProgress(accountId, topicId)).thenReturn(topicProgress);
        when(progressService.getProgressByTopic(accountId, topicId)).thenReturn(progressResponse);
        when(lessonBuilder.buildCompleteLessonResponse(eq(false), eq(0), eq(topicProgress), eq(progressResponse)))
                .thenReturn(expectedResponse);

        CompleteLessonResponse result = lessonService.completeLesson(accountId, lessonId, request);

        assertThat(result.getXpEarned()).isEqualTo(0);

        verify(progressService, never()).addXP(any(), any(), any());
        verify(progressService, never()).incrementLessonCompletion(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when lesson not found on complete")
    void shouldThrowExceptionWhenLessonNotFoundOnComplete() {
        CompleteLessonRequest request = new CompleteLessonRequest();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("error.lesson.notfound"), isNull(), any(Locale.class))).thenReturn("Lesson not found");

        assertThatThrownBy(() -> lessonService.completeLesson(accountId, lessonId, request))
                .isInstanceOf(LessonNotFoundException.class)
                .hasMessage("Lesson not found");

        verify(lessonRepository).findById(lessonId);
    }

    @Test
    @DisplayName("Should throw exception when progress not found on complete")
    void shouldThrowExceptionWhenProgressNotFoundOnComplete() {
        CompleteLessonRequest request = new CompleteLessonRequest();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId))
                .thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("error.lesson.progress.notfound"), isNull(), any(Locale.class))).thenReturn("Lesson progress not found");

        assertThatThrownBy(() -> lessonService.completeLesson(accountId, lessonId, request))
                .isInstanceOf(LessonNotFoundException.class)
                .hasMessage("Lesson progress not found");
    }

    @Test
    @DisplayName("Should create lesson successfully and return FlashcardLessonResponse")
    void shouldCreateLessonSuccessfully() {
        FlashcardLessonRequest request = LessonTestFactory.createFlashcardLessonRequest(topicId, "New Lesson");
        Topic topic = LessonTestFactory.createTopic(topicId, "Topic");
        FlashcardLesson lessonEntity = LessonTestFactory.createFlashcardLesson(null, topic, "New Lesson");
        FlashcardLessonResponse expectedResponse = LessonTestFactory.createFlashcardLessonResponse(UUID.randomUUID(), "New Lesson");

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(lessonRequestToLessonEntity.lessonRequestToLessonEntity(eq(request), any())).thenReturn(lessonEntity);
        when(lessonRepository.save(any())).thenReturn(lessonEntity);
        when(lessonEntityToLessonResponse.lessonEntityToLessonResponse(any(), any())).thenReturn(expectedResponse);

        LessonResponse result = lessonService.createLesson(request);

        assertThat(result).isInstanceOf(FlashcardLessonResponse.class);
        assertThat(result.getTitle()).isEqualTo("New Lesson");
        assertThat(((FlashcardLessonResponse)result).getFlashcards()).isNotNull();
        verify(lessonRepository).save(any());
    }

    @Test
    @DisplayName("Should create MatchingPairLesson successfully and return MatchingPairLessonResponse")
    void shouldCreateMatchingPairLessonSuccessfully() {
        MatchingPairLessonRequest request = LessonTestFactory.createMatchingPairLessonRequest(topicId, "Matching Pair Lesson");
        Topic topic = LessonTestFactory.createTopic(topicId, "Topic");
        MatchingPairLesson lessonEntity = LessonTestFactory.createMatchingPairLesson(null, topic, "Matching Pair Lesson");
        MatchingPairLessonResponse expectedResponse = LessonTestFactory.createMatchingPairLessonResponse(UUID.randomUUID(), "Matching Pair Lesson");

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(lessonRequestToLessonEntity.lessonRequestToLessonEntity(eq(request), any())).thenReturn(lessonEntity);
        when(lessonRepository.save(any())).thenReturn(lessonEntity);
        when(lessonEntityToLessonResponse.lessonEntityToLessonResponse(any(), any())).thenReturn(expectedResponse);

        LessonResponse result = lessonService.createLesson(request);

        assertThat(result).isInstanceOf(MatchingPairLessonResponse.class);
        assertThat(result.getTitle()).isEqualTo("Matching Pair Lesson");
        assertThat(((MatchingPairLessonResponse)result).getMatchingPairs()).isNotNull();
        verify(lessonRepository).save(any());
    }

    @Test
    @DisplayName("Should create QcmLesson successfully and return QcmLessonResponse")
    void shouldCreateQcmLessonSuccessfully() {
        QcmLessonRequest request = LessonTestFactory.createQcmLessonRequest(topicId, "QCM Lesson");
        Topic topic = LessonTestFactory.createTopic(topicId, "Topic");
        QcmLesson lessonEntity = LessonTestFactory.createQcmLesson(null, topic, "QCM Lesson");
        QcmLessonResponse expectedResponse = LessonTestFactory.createQcmLessonResponse(UUID.randomUUID(), "QCM Lesson");

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(lessonRequestToLessonEntity.lessonRequestToLessonEntity(eq(request), any())).thenReturn(lessonEntity);
        when(lessonRepository.save(any())).thenReturn(lessonEntity);
        when(lessonEntityToLessonResponse.lessonEntityToLessonResponse(any(), any())).thenReturn(expectedResponse);

        LessonResponse result = lessonService.createLesson(request);

        assertThat(result).isInstanceOf(QcmLessonResponse.class);
        assertThat(result.getTitle()).isEqualTo("QCM Lesson");
        assertThat(((QcmLessonResponse)result).getQuestions()).isNotNull();
        verify(lessonRepository).save(any());
    }

    @Test
    @DisplayName("Should create SortingExerciseLesson successfully and return SortingExerciseLessonResponse")
    void shouldCreateSortingExerciseLessonSuccessfully() {
        SortingExerciseLessonRequest request = LessonTestFactory.createSortingExerciseLessonRequest(topicId, "Sorting Exercise Lesson");
        Topic topic = LessonTestFactory.createTopic(topicId, "Topic");
        SortingExerciseLesson lessonEntity = LessonTestFactory.createSortingExerciseLesson(null, topic, "Sorting Exercise Lesson");
        SortingExerciseLessonResponse expectedResponse = LessonTestFactory.createSortingExerciseLessonResponse(UUID.randomUUID(), "Sorting Exercise Lesson");

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(lessonRequestToLessonEntity.lessonRequestToLessonEntity(eq(request), any())).thenReturn(lessonEntity);
        when(lessonRepository.save(any())).thenReturn(lessonEntity);
        when(lessonEntityToLessonResponse.lessonEntityToLessonResponse(any(), any())).thenReturn(expectedResponse);

        LessonResponse result = lessonService.createLesson(request);

        assertThat(result).isInstanceOf(SortingExerciseLessonResponse.class);
        assertThat(result.getTitle()).isEqualTo("Sorting Exercise Lesson");
        assertThat(((SortingExerciseLessonResponse)result).getSortingExercise()).isNotNull();
        verify(lessonRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when topic not found on create lesson")
    void shouldThrowTopicNotFoundOnCreateLesson() {
        FlashcardLessonRequest request = LessonTestFactory.createFlashcardLessonRequest(topicId, "New Lesson");
        when(topicRepository.findById(topicId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("error.topic.notfound"), isNull(), any(Locale.class))).thenReturn("Topic not found");

        assertThatThrownBy(() -> lessonService.createLesson(request))
                .isInstanceOf(TopicNotFoundException.class)
                .hasMessage("Topic not found");
    }

    @Test
    @DisplayName("Should update lesson successfully")
    void shouldUpdateLessonSuccessfully() {
        FlashcardLessonRequest request = LessonTestFactory.createFlashcardLessonRequest(topicId, "Updated Lesson");
        Topic topic = LessonTestFactory.createTopic(topicId, "Topic");
        FlashcardLesson existingLesson = LessonTestFactory.createFlashcardLesson(lessonId, topic, "Old Lesson");
        LessonResponse expectedResponse = LessonResponse.builder().id(lessonId).title("Updated Lesson").build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(existingLesson));
        doNothing().when(lessonRequestToLessonEntity).updateLessonFromRequest(eq(request), eq(existingLesson), any());
        when(lessonRepository.save(any())).thenReturn(existingLesson);
        when(lessonEntityToLessonResponse.lessonEntityToLessonResponse(any(), any())).thenReturn(expectedResponse);

        LessonResponse result = lessonService.updateLesson(lessonId, request);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Lesson");
        verify(lessonRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when lesson not found on update")
    void shouldThrowLessonNotFoundOnUpdate() {
        FlashcardLessonRequest request = LessonTestFactory.createFlashcardLessonRequest(topicId, "Updated Lesson");
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("error.lesson.notfound"), isNull(), any(Locale.class))).thenReturn("Lesson not found");

        assertThatThrownBy(() -> lessonService.updateLesson(lessonId, request))
                .isInstanceOf(LessonNotFoundException.class)
                .hasMessage("Lesson not found");
    }
}

