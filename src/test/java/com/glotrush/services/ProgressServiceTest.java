package com.glotrush.services;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.glotrush.config.TestMessageSourceConfig;
import com.glotrush.utils.LevelUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.glotrush.builder.ProgressBuilder;
import com.glotrush.dto.response.ProgressOverviewResponse;
import com.glotrush.dto.response.UserProgressResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Language;
import com.glotrush.entities.Topic;
import com.glotrush.entities.UserProgress;
import com.glotrush.exceptions.ResourceNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.UserLanguageRepository;
import com.glotrush.repositories.UserLessonProgressRepository;
import com.glotrush.repositories.UserProgressRepository;
import com.glotrush.services.progress.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(classes = TestMessageSourceConfig.class)
@DisplayName("ProgressService Unit Tests")
class ProgressServiceTest {
    @Autowired
    private MessageSource messageSource;
    private ProgressService progressService;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private UserProgressRepository userProgressRepository;

    @Mock
    private ProgressBuilder progressBuilder;

    @Mock
    private LessonRepository lessonRepository;

    @Mock    
    private UserLanguageRepository userLanguageRepository;


    @Mock
    private UserLessonProgressRepository userLessonProgressRepository;

    private UUID accountId;
    private UUID topicId;
    private UUID languageId;
    private Accounts testAccount;
    private Topic testTopic;
    private Language testLanguage;
    private UserProgress testProgress;

     @BeforeEach
    void setUp() {
        progressService = new ProgressService(messageSource, userProgressRepository, topicRepository, accountsRepository, progressBuilder, lessonRepository, userLessonProgressRepository, userLanguageRepository);
        accountId = UUID.randomUUID();
        topicId = UUID.randomUUID();
        languageId = UUID.randomUUID();

        testAccount = Accounts.builder()
                .id(accountId)
                .email("test@example.com")
                .build();

        testLanguage = Language.builder()
                .id(languageId)
                .code("EN")
                .name("English")
                .build();

        testTopic = Topic.builder()
                .id(topicId)
                .name("Grammar Basics")
                .language(testLanguage)
                .build();

        testProgress = UserProgress.builder()
                .id(UUID.randomUUID())
                .account(testAccount)
                .topic(testTopic)
                .totalXP(500L)
                .completedLessons(5)
                .completionPercentage(50.0)
                .correctAnswers(80)
                .totalAnswers(100)
                .accuracy(0.8)
                .studyStreak(3)
                .build();
    }
    
    @Test
    @DisplayName("Should return progress overview successfully")
    void shouldGetProgressOverviewSuccessfully() {
        UserProgressResponse progressResponse = new UserProgressResponse();
        ProgressOverviewResponse expectedResponse = ProgressOverviewResponse.builder()
                .totalXP(500L)
                .overallLevel(1)
                .totalTopicsStarted(1)
                .totalLessonsCompleted(5)
                .overallAccuracy(0.8)
                .currentStreak(3)
                .progressByTopic(List.of(progressResponse))
                .build();

        when(userProgressRepository.findByAccount_Id(accountId)).thenReturn(List.of(testProgress));
        when(progressBuilder.mapToUserProgressResponse(testProgress)).thenReturn(progressResponse);
        when(progressBuilder.buildProgressOverview(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(expectedResponse);

        ProgressOverviewResponse result = progressService.getProgressOverview(accountId);

        assertThat(result).isNotNull();
        assertThat(result.getTotalXP()).isEqualTo(500L);
        assertThat(result.getTotalTopicsStarted()).isEqualTo(1);
        assertThat(result.getTotalLessonsCompleted()).isEqualTo(5);

        verify(userProgressRepository, times(2)).findByAccount_Id(accountId);
    }

    @Test
    @DisplayName("Should return empty overview when no progress exists")
    void shouldReturnEmptyOverviewWhenNoProgress() {
        ProgressOverviewResponse expectedResponse = ProgressOverviewResponse.builder()
                .totalXP(0L)
                .overallLevel(1)
                .totalTopicsStarted(0)
                .totalLessonsCompleted(0)
                .overallAccuracy(0.0)
                .currentStreak(0)
                .progressByTopic(Collections.emptyList())
                .build();

        when(userProgressRepository.findByAccount_Id(accountId)).thenReturn(Collections.emptyList());
        when(progressBuilder.buildProgressOverview(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(expectedResponse);

        ProgressOverviewResponse result = progressService.getProgressOverview(accountId);

        assertThat(result).isNotNull();
        assertThat(result.getTotalXP()).isEqualTo(0L);
        assertThat(result.getTotalTopicsStarted()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return progress by topic successfully")
    void shouldGetProgressByTopicSuccessfully() {
        UserProgressResponse expectedResponse = UserProgressResponse.builder()
                .topicId(topicId)
                .topicName("Grammar Basics")
                .level(1)
                .totalXP(500L)
                .build();

        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.of(testProgress));
        when(progressBuilder.mapToUserProgressResponse(testProgress)).thenReturn(expectedResponse);

        UserProgressResponse result = progressService.getProgressByTopic(accountId, topicId);

        assertThat(result).isNotNull();
        assertThat(result.getTopicId()).isEqualTo(topicId);
        assertThat(result.getLevel()).isEqualTo(1);

        verify(userProgressRepository).findByAccount_IdAndTopic_Id(accountId, topicId);
    }

    @Test
    @DisplayName("Should throw exception when progress not found for topic")
    void shouldThrowExceptionWhenProgressNotFound() {
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressService.getProgressByTopic(accountId, topicId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Progress not found for this topic");

        verify(userProgressRepository).findByAccount_IdAndTopic_Id(accountId, topicId);
    }

    @Test
    @DisplayName("Should return progress by language successfully")
    void shouldGetProgressByLanguageSuccessfully() {
        UserProgressResponse expectedResponse = new UserProgressResponse();

        when(userProgressRepository.findByAccount_IdAndTopic_Language_Id(accountId, languageId))
                .thenReturn(List.of(testProgress));
        when(progressBuilder.mapToUserProgressResponse(testProgress)).thenReturn(expectedResponse);

        List<UserProgressResponse> result = progressService.getProgressByLanguage(accountId, languageId);

        assertThat(result).hasSize(1);
        verify(userProgressRepository).findByAccount_IdAndTopic_Language_Id(accountId, languageId);
    }

    @Test
    @DisplayName("Should return existing progress when found")
    void shouldReturnExistingProgress() {
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.of(testProgress));

        UserProgress result = progressService.getOrCreateProgress(accountId, topicId);

        assertThat(result).isEqualTo(testProgress);
        verifyNoInteractions(accountsRepository);
        verifyNoInteractions(topicRepository);
    }

    @Test
    @DisplayName("Should create new progress when not found")
    void shouldCreateNewProgressWhenNotFound() {
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.empty());
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(userProgressRepository.save(any(UserProgress.class))).thenAnswer(i -> i.getArgument(0));

        UserProgress result = progressService.getOrCreateProgress(accountId, topicId);

        assertThat(result).isNotNull();
        assertThat(result.getTotalXP()).isEqualTo(0L);
        assertThat(result.getExamPassed()).isFalse();
        assertThat(LevelUtils.calculateLevel(result.getTotalXP())).isEqualTo(1);

        verify(userProgressRepository).save(any(UserProgress.class));
    }

    @Test
    @DisplayName("Should throw exception when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.empty());
        when(accountsRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressService.getOrCreateProgress(accountId, topicId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Account not found");
    }

    @Test
    @DisplayName("Should throw exception when topic not found")
    void shouldThrowExceptionWhenTopicNotFound() {
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.empty());
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressService.getOrCreateProgress(accountId, topicId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Topic not found");
    }

    @Test
    @DisplayName("Should add XP without level up")
    void shouldAddXPWithoutLevelUp() {
        testProgress.setTotalXP(500L);

        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.of(testProgress));
        when(userProgressRepository.save(any(UserProgress.class))).thenAnswer(i -> i.getArgument(0));

        UserProgress result = progressService.addXP(accountId, topicId, 50);

        assertThat(result.getTotalXP()).isEqualTo(550L);
        assertThat(LevelUtils.calculateLevel(result.getTotalXP())).isEqualTo(3);

        verify(userProgressRepository).save(any(UserProgress.class));
    }

    @Test
    @DisplayName("Should add XP and level up")
    void shouldAddXPAndLevelUp() {
        testProgress.setTotalXP(950L);

        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.of(testProgress));
        when(userProgressRepository.save(any(UserProgress.class))).thenAnswer(i -> i.getArgument(0));

        UserProgress result = progressService.addXP(accountId, topicId, 100);

        assertThat(result.getTotalXP()).isEqualTo(1050L);
        assertThat(LevelUtils.calculateLevel(result.getTotalXP())).isEqualTo(5);
        assertThat(LevelUtils.calculateCurrentLevelXP(result.getTotalXP())).isEqualTo(50L);
    }

    @Test
    @DisplayName("Should increment lesson completion")
    void shouldIncrementLessonCompletion() {
        testProgress.setCompletedLessons(4);

        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.of(testProgress));
        when(lessonRepository.countByTopic_Id(topicId)).thenReturn(10);
        when(userProgressRepository.save(any(UserProgress.class))).thenAnswer(i -> i.getArgument(0));

        UserProgress result = progressService.incrementLessonCompletion(accountId, topicId);

        assertThat(result.getCompletedLessons()).isEqualTo(5);
        assertThat(result.getCompletionPercentage()).isEqualTo(50.0);

        verify(userProgressRepository).save(any(UserProgress.class));
        verify(lessonRepository).countByTopic_Id(topicId);
    }

    @Test
    @DisplayName("Should update answer stats")
    void shouldUpdateAnswerStats() {
        testProgress.setCorrectAnswers(80);
        testProgress.setTotalAnswers(100);

        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.of(testProgress));
        when(userProgressRepository.save(any(UserProgress.class))).thenAnswer(i -> i.getArgument(0));

        UserProgress result = progressService.updateAnswerStats(accountId, topicId, 18, 20);

        assertThat(result.getCorrectAnswers()).isEqualTo(98);
        assertThat(result.getTotalAnswers()).isEqualTo(120);

        verify(userProgressRepository).save(any(UserProgress.class));
    }


    @Test
    @DisplayName("Should update last studied")
    void shouldUpdateLastStudiedAt() {
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.of(testProgress));
        when(userProgressRepository.save(any(UserProgress.class))).thenAnswer(i -> i.getArgument(0));

        UserProgress result = progressService.updateLastStudiedAt(accountId, topicId);

        assertThat(result.getLastStudiedAt()).isNotNull();
        verify(userProgressRepository).save(any(UserProgress.class));
    }
}
