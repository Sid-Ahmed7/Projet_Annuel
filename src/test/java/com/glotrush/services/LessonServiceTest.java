package com.glotrush.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.glotrush.config.TestMessageSourceConfig;
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
import com.glotrush.dto.response.UserLessonProgressSummary;
import com.glotrush.dto.response.UserProgressResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Topic;
import com.glotrush.entities.UserLessonProgress;
import com.glotrush.entities.UserProgress;
import com.glotrush.enumerations.LessonStatus;
import com.glotrush.exceptions.LessonNotFoundException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.entities.lesson.FlashcardLesson;
import com.glotrush.entities.lesson.MatchingPairLesson;
import com.glotrush.entities.lesson.QcmLesson;
import com.glotrush.entities.lesson.SortingExerciseLesson;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.UserLessonProgressRepository;
import com.glotrush.services.lesson.LessonService;
import com.glotrush.services.progress.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(classes = TestMessageSourceConfig.class)
@DisplayName("LessonService Unit Tests")
class LessonServiceTest {
    @Autowired
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

    private LessonService lessonService;

    private UUID accountId;
    private UUID lessonId;
    private UUID topicId;
    private Accounts account;
    private FlashcardLesson lesson;
    private UserLessonProgress userLessonProgress;

    @BeforeEach
    void setUp() {
        lessonService = new LessonService(messageSource, lessonRepository, userLessonProgressRepository, accountsRepository, progressService, lessonBuilder, topicRepository, lessonEntityToLessonResponse, lessonRequestToLessonEntity);
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
                .totalLessons(10)
                .build();

        lesson = FlashcardLesson.builder()
                .id(lessonId)
                .title("Introduction to Spring")
                .description("Learn about Spring framework basics")
                .topic(topic)
                .xpReward(50)
                .orderIndex(1)
                .isActive(true)
                .isLocked(false)
                .minLevelRequired(1)
                .durationMinutes(15)
                .build();

        userLessonProgress = UserLessonProgress.builder()
          .id(UUID.randomUUID())
                .account(account)
                .lesson(lesson)
                .status(LessonStatus.NOT_STARTED)
                .attempts(0)
                .score(20.00)
                .timeSpentSeconds(0)
                .build();
    }


    @Test
    @DisplayName("Should return all active lessons for topic")
    void shouldGetLessonsByTopic() {
        LessonResponse expectedResponse = LessonResponse.builder()
                .id(lessonId)
                .title("Introduction to Spring")
                .build();

        when(lessonRepository.findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topicId))
                .thenReturn(List.of(lesson));
        when(userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId))
                .thenReturn(Optional.of(userLessonProgress));
        when(lessonBuilder.mapLessonToLessonResponse(eq(lesson), any(), any()))
                .thenReturn(expectedResponse);

        List<LessonResponse> result = lessonService.getLessonsByTopic(topicId, accountId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Introduction to Spring");

        verify(lessonRepository).findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topicId);
    }

    @Test
    @DisplayName("Should return empty list when no lessons for topic")
    void shouldReturnEmptyListWhenNoLessonsForTopic() {
        when(lessonRepository.findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topicId))
                .thenReturn(Collections.emptyList());

        List<LessonResponse> result = lessonService.getLessonsByTopic(topicId, accountId);

        assertThat(result).isEmpty();
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
        when(userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId))
                .thenReturn(Optional.of(userLessonProgress));
        when(lessonBuilder.mapLessonToLessonResponse(eq(lesson), any(), any()))
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
                .attempts(0)
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

        assertThatThrownBy(() -> lessonService.startLesson(accountId, lessonId))
                .isInstanceOf(LessonNotFoundException.class)
                .hasMessage("Lesson not found");
    }

    @Test
    @DisplayName("Should complete lesson first time and award XP")
    void shouldCompleteLessonFirstTimeAndAwardXP() {
        CompleteLessonRequest request = new CompleteLessonRequest();
        request.setScore(85.00);
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
        when(lessonBuilder.buildCompleteLessonResponse(eq(false), eq(50), any(), any(), eq(1)))
                .thenReturn(expectedResponse);

        CompleteLessonResponse result = lessonService.completeLesson(accountId, lessonId, request);

        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getXpEarned()).isEqualTo(50);

        verify(userLessonProgressRepository).save(any(UserLessonProgress.class));
        verify(progressService).addXP(accountId, topicId, 50);
        verify(progressService).updateLastStudiedAt(accountId, topicId);
        verify(progressService).incrementLessonCompletion(accountId, topicId);
    }

    @Test
    @DisplayName("Should complete lesson with level up")
    void shouldCompleteLessonWithLevelUp() {
        CompleteLessonRequest request = new CompleteLessonRequest();
        request.setScore(100.00);
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
                .newLevel(2)
                .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId))
                .thenReturn(Optional.of(userLessonProgress));
        when(progressService.getOrCreateProgress(accountId, topicId)).thenReturn(topicProgressBefore);
        when(progressService.updateLastStudiedAt(accountId, topicId)).thenReturn(topicProgressAfter);  
        when(progressService.addXP(accountId, topicId, 50)).thenReturn(topicProgressAfter);
        when(progressService.incrementLessonCompletion(accountId, topicId)).thenReturn(topicProgressAfter);
        when(progressService.getProgressByTopic(accountId, topicId)).thenReturn(progressResponse);
        when(lessonBuilder.buildCompleteLessonResponse(eq(true), eq(50), any(), any(), eq(2)))
                .thenReturn(expectedResponse);

        CompleteLessonResponse result = lessonService.completeLesson(accountId, lessonId, request);

        verify(progressService).updateLastStudiedAt(accountId, topicId);
        assertThat(result.getLeveledUp()).isTrue();
        assertThat(result.getNewLevel()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle lesson recompletion without XP")
    void shouldHandleLessonRecompletionWithoutXP() {
        CompleteLessonRequest request = new CompleteLessonRequest();
        request.setScore(90.00);
        request.setTimeSpentSeconds(500);

        userLessonProgress.setStatus(LessonStatus.COMPLETED);
        userLessonProgress.setAttempts(1);

        UserProgressResponse progressResponse = new UserProgressResponse();

        CompleteLessonResponse expectedResponse = CompleteLessonResponse.builder()
                .success(true)
                .xpEarned(0)
                .message("Lesson already completed. No XP awarded.")
                .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId))
                .thenReturn(Optional.of(userLessonProgress));
        when(progressService.getProgressByTopic(accountId, topicId)).thenReturn(progressResponse);
        when(lessonBuilder.buildRecompletedLessonResponse(any())).thenReturn(expectedResponse);

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

        assertThatThrownBy(() -> lessonService.completeLesson(accountId, lessonId, request))
                .isInstanceOf(LessonNotFoundException.class)
                .hasMessage("Lesson not found");
    }

    @Test
    @DisplayName("Should throw exception when progress not found on complete")
    void shouldThrowExceptionWhenProgressNotFoundOnComplete() {
        CompleteLessonRequest request = new CompleteLessonRequest();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lessonId))
                .thenReturn(Optional.empty());

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
        assertThat(((MatchingPairLessonResponse)result).getMatchingPairResponses()).isNotNull();
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
        assertThat(((QcmLessonResponse)result).getQcmQuestionResponses()).isNotNull();
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
        assertThat(((SortingExerciseLessonResponse)result).getSortingExerciseResponses()).isNotNull();
        verify(lessonRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when topic not found on create lesson")
    void shouldThrowTopicNotFoundOnCreateLesson() {
        FlashcardLessonRequest request = LessonTestFactory.createFlashcardLessonRequest(topicId, "New Lesson");
        when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.createLesson(request))
                .isInstanceOf(TopicNotFoundException.class);
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

        assertThatThrownBy(() -> lessonService.updateLesson(lessonId, request))
                .isInstanceOf(LessonNotFoundException.class);
    }
}

