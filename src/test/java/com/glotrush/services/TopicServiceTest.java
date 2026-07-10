package com.glotrush.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.glotrush.config.TestMessageSourceConfig;
import com.glotrush.dispatcher.notifications.NotificationDispatcher;
import com.glotrush.entities.*;
import com.glotrush.enumerations.ProficiencyLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.glotrush.builder.LessonBuilder;
import com.glotrush.builder.TopicBuilder;
import com.glotrush.dto.response.TopicProgressResponse;
import com.glotrush.dto.response.TopicResponse;
import com.glotrush.dto.response.TopicWithProgressResponse;
import com.glotrush.entities.exercice.FlashcardEntity;
import com.glotrush.entities.exercice.InteractiveQuestionEntity;
import com.glotrush.entities.exercice.QcmQuestionEntity;
import com.glotrush.exceptions.TopicNotFoundException;
import com.glotrush.dto.request.ExamResultRequest;
import com.glotrush.dto.request.FlashcardAnswerRequest;
import com.glotrush.dto.request.InteractiveAnswerRequest;
import com.glotrush.dto.request.QcmAnswerRequest;
import com.glotrush.enumerations.InteractiveSystemType;
import com.glotrush.dto.request.TopicRequest;
import com.glotrush.mapping.TopicMapper;
import com.glotrush.repositories.LanguageRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.UserLessonProgressRepository;
import com.glotrush.repositories.UserProgressRepository;
import com.glotrush.services.topic.TopicService;
import com.glotrush.services.progress.IProgressService;
import com.glotrush.services.rewiewMistake.IReviewMistakeService;
import com.glotrush.mapping.LessonEntityToLessonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.glotrush.repositories.exercice.FlashcardRepository;
import com.glotrush.repositories.exercice.InteractiveQuestionRepository;
import com.glotrush.repositories.exercice.MatchingPairRepository;
import com.glotrush.repositories.exercice.QcmQuestionRepository;
import com.glotrush.repositories.exercice.SortingExerciseRepository;
import com.glotrush.dto.response.CompleteExamResponse;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(classes = TestMessageSourceConfig.class)
@DisplayName("TopicService Unit Tests")
class TopicServiceTest {
    @Autowired
    private MessageSource messageSource;
    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserProgressRepository userProgressRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private TopicBuilder topicBuilder;

    @Mock
    private TopicMapper topicMapper;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private LessonEntityToLessonResponse lessonMapper;

    @Mock
    private UserLessonProgressRepository userLessonProgressRepository;

    @Mock
    private IProgressService progressService;

    @Mock
    private FlashcardRepository flashcardRepository;
    @Mock
    private QcmQuestionRepository qcmQuestionRepository;
    @Mock
    private MatchingPairRepository matchingPairRepository;
    @Mock
    private SortingExerciseRepository sortingExerciseRepository;
    @Mock
    private InteractiveQuestionRepository interactiveQuestionRepository;

    @Mock
    private LessonBuilder lessonBuilder;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private IReviewMistakeService reviewMistakeService;

    private TopicService topicService;

    private UUID accountId;
    private UUID topicId;
    private UUID languageId;
    private Topic topic;
    private Language language;
    private UserProgress userProgress;

    @BeforeEach
    void setUp() {
        topicService = new TopicService(
                messageSource,
                topicRepository,
                userProgressRepository,
                languageRepository,
                lessonRepository,
                userLessonProgressRepository,
                lessonBuilder,
                topicBuilder,
                topicMapper,
                lessonMapper,
                notificationDispatcher,
                reviewMistakeService,
                flashcardRepository,
                qcmQuestionRepository,
                matchingPairRepository,
                sortingExerciseRepository,
                interactiveQuestionRepository
        );
        accountId = UUID.randomUUID();
        topicId = UUID.randomUUID();
        languageId = UUID.randomUUID();

        language = Language.builder()
                .id(languageId)
                .code("EN")
                .name("English")
                .build();

        topic = Topic.builder()
                .id(topicId)
                .name("Basics")
                .description("Basics Course")
                .targetLanguage(language)
                .sourceLanguage(language)
                .isActive(true)
                .build();

        userProgress = UserProgress.builder()
                .id(UUID.randomUUID())
                .topic(topic)
                .totalXP(500L)
                .completedLessons(5)
                .completionPercentage(40.0)
                .build();
    }
    
    @Test
    @DisplayName("Should return all topics without progress")
    void shouldGetAllTopicsWithoutProgress() {
        TopicResponse expectedResponse = TopicResponse.builder()
                .id(topicId)
                .name("Basics")
                .build();

        when(topicRepository.findAll()).thenReturn(List.of(topic));
        when(topicMapper.mapTopicEntitiesToTopicResponse(topic)).thenReturn(expectedResponse);

        List<TopicResponse> result = topicService.getAllTopics();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Basics");

        verify(topicRepository).findAll();
    }

    @Test
    @DisplayName("Should return all active topics with user progress")
    void shouldGetAllTopicsWithProgress() {
        TopicResponse expectedResponse = TopicResponse.builder()
                .id(topicId)
                .name("Basics Course")
                .build();

        when(topicRepository.findByIsActiveTrueOrderByDifficultyAscNameAsc()).thenReturn(List.of(topic));
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.of(userProgress));
        when(topicBuilder.mapToTopicResponse(eq(topic), any())).thenReturn(expectedResponse);

        List<TopicResponse> result = topicService.getAllTopics(accountId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Basics Course");

        verify(topicRepository).findByIsActiveTrueOrderByDifficultyAscNameAsc();
    }

    @Test
    @DisplayName("Should return topics without progress for new user")
    void shouldReturnTopicsWithoutProgressForNewUser() {
        TopicResponse expectedResponse = TopicResponse.builder()
                .id(topicId)
                .name("Basics Course")
                .build();

        when(topicRepository.findByIsActiveTrueOrderByDifficultyAscNameAsc()).thenReturn(List.of(topic));
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.empty());
        when(topicBuilder.mapToTopicResponse(eq(topic), eq(Optional.empty()))).thenReturn(expectedResponse);

        List<TopicResponse> result = topicService.getAllTopics(accountId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should return empty list when no active topics")
    void shouldReturnEmptyListWhenNoActiveTopics() {
        when(topicRepository.findByIsActiveTrueOrderByDifficultyAscNameAsc()).thenReturn(Collections.emptyList());

        List<TopicResponse> result = topicService.getAllTopics(accountId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return topics for specific language")
    void shouldGetTopicsByLanguage() {
        TopicWithProgressResponse expectedResponse = TopicWithProgressResponse.builder()
                .id(topicId)
                .name("Basics Course")
                .targetLanguageId(languageId)
                .sourceLanguageId(languageId)
                .build();

        when(topicRepository.findByTargetLanguage_IdAndIsActiveTrueOrderByDifficultyAscNameAsc(languageId))
                .thenReturn(List.of(topic));
        when(lessonRepository.findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topicId))
                .thenReturn(Collections.emptyList());
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.of(userProgress));
        when(topicBuilder.mapToTopicWithProgressResponse(eq(topic), any(), anyInt(), anyBoolean(), anyBoolean()))
                .thenReturn(expectedResponse);

        List<TopicWithProgressResponse> result = topicService.getTopicsByLanguage(languageId, accountId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetLanguageId()).isEqualTo(languageId);

        verify(topicRepository).findByTargetLanguage_IdAndIsActiveTrueOrderByDifficultyAscNameAsc(languageId);
    }

    @Test
    @DisplayName("Should return empty list when no topics for language")
    void shouldReturnEmptyListWhenNoTopicsForLanguage() {
        when(topicRepository.findByTargetLanguage_IdAndIsActiveTrueOrderByDifficultyAscNameAsc(languageId))
                .thenReturn(Collections.emptyList());

        List<TopicWithProgressResponse> result = topicService.getTopicsByLanguage(languageId, accountId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return topic by id with user progress")
    void shouldGetTopicByIdWithProgress() {
        TopicResponse expectedResponse = TopicResponse.builder()
                .id(topicId)
                .name("Grammar Basics")
                .build();

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId))
                .thenReturn(Optional.of(userProgress));
        when(topicBuilder.mapToTopicResponse(eq(topic), any())).thenReturn(expectedResponse);

        TopicResponse result = topicService.getTopicById(topicId, accountId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(topicId);

        verify(topicRepository).findById(topicId);
    }

    @Test
    @DisplayName("Should throw exception when topic not found")
    void shouldThrowExceptionWhenTopicNotFound() {
        when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicService.getTopicById(topicId, accountId))
                .isInstanceOf(TopicNotFoundException.class);

        verify(topicRepository).findById(topicId);
    }

    @Test
    @DisplayName("Should create topic successfully")
    void shouldCreateTopicSuccessfully() {
        TopicRequest request = TopicRequest.builder()
                .targetLanguageId(languageId)
                .sourceLanguageId(languageId)
                .name("New Topic")
                .build();
        TopicResponse expectedResponse = TopicResponse.builder().id(topicId).name("New Topic").build();

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(topicMapper.mapTopicRequestToMapTopicEntities(any())).thenReturn(topic);
        when(topicMapper.mapTopicEntitiesToTopicResponse(any())).thenReturn(expectedResponse);

        TopicResponse result = topicService.createTopic(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Topic");
        verify(topicRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating topic with invalid target language")
    void shouldThrowExceptionWhenCreatingTopicWithInvalidTargetLanguage() {
        TopicRequest request = TopicRequest.builder()
                .targetLanguageId(languageId)
                .sourceLanguageId(languageId)
                .build();
        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicService.createTopic(request))
                .isInstanceOf(TopicNotFoundException.class);
    }

    @Test
    @DisplayName("Should disable topic successfully")
    void shouldDisableTopicSuccessfully() {
        when(topicRepository.existsById(topicId)).thenReturn(true);
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        topicService.disableTopic(topicId);

        verify(topicRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when disabling non-existent topic")
    void shouldThrowExceptionWhenDisablingNonExistentTopic() {
        when(topicRepository.existsById(topicId)).thenReturn(false);

        assertThatThrownBy(() -> topicService.disableTopic(topicId))
                .isInstanceOf(TopicNotFoundException.class);
    }

    @Test
    @DisplayName("Should update topic successfully")
    void shouldUpdateTopicSuccessfully() {
        TopicRequest request = TopicRequest.builder()
                .targetLanguageId(languageId)
                .sourceLanguageId(languageId)
                .name("Updated Name")
                .build();
        TopicResponse expectedResponse = TopicResponse.builder().id(topicId).name("Updated Name").build();

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        doNothing().when(topicMapper).updateTopicFromRequest(eq(request), eq(topic));
        when(topicMapper.mapTopicEntitiesToTopicResponse(any())).thenReturn(expectedResponse);
        when(topicRepository.save(any())).thenReturn(topic);

        TopicResponse result = topicService.updateTopic(topicId, request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        verify(topicRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent topic")
    void shouldThrowExceptionWhenUpdatingNonExistentTopic() {
        TopicRequest request = TopicRequest.builder()
                .targetLanguageId(languageId)
                .sourceLanguageId(languageId)
                .build();
        when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicService.updateTopic(topicId, request))
                .isInstanceOf(TopicNotFoundException.class);
    }

    @Test
    @DisplayName("Should search topics with all filters")
    void shouldSearchTopicsWithAllFilters() {
        TopicResponse response = TopicResponse.builder().id(topicId).name("Basics").build();
        when(topicRepository.findAll(any(Specification.class))).thenReturn(List.of(topic));
        when(topicMapper.mapTopicEntitiesToTopicResponse(topic)).thenReturn(response);

        List<TopicResponse> result = topicService.searchTopics("Basics", ProficiencyLevel.A1, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Basics");
    }

    @Test
    @DisplayName("Should search active topics with language filter")
    void shouldSearchActiveTopicsWithLanguageFilter() {
        TopicResponse response = TopicResponse.builder().id(topicId).name("Basics").build();
        when(topicRepository.findAll(any(Specification.class))).thenReturn(List.of(topic));
        when(topicMapper.mapTopicEntitiesToTopicResponse(topic)).thenReturn(response);

        List<TopicResponse> result = topicService.searchActiveTopics(languageId, "Basics", ProficiencyLevel.A1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Basics");
        verify(topicRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should search active topics paginated")
    void shouldSearchActiveTopicsPaged() {
        TopicResponse response = TopicResponse.builder().id(topicId).name("Basics").build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Topic> page = new PageImpl<>(List.of(topic), pageable, 1);

        when(topicRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(topicMapper.mapTopicEntitiesToTopicResponse(topic)).thenReturn(response);

        Page<TopicResponse> result = topicService.searchActiveTopics(languageId, "Basics", ProficiencyLevel.A1, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Basics");
        verify(topicRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should get active topics progress")
    void shouldGetActiveTopicsProgress() {
        Pageable pageable = PageRequest.of(0, 3);
        LocalDateTime studiedAt = LocalDateTime.now();
        UserProgress progress = UserProgress.builder()
                .topic(topic)
                .completionPercentage(45.0)
                .lastStudiedAt(studiedAt)
                .build();
        Page<Topic> topicPage = new PageImpl<>(List.of(topic), pageable, 1);

        when(topicRepository.findActiveTopicsWithProgress(eq(languageId), eq(accountId), eq(pageable)))
                .thenReturn(topicPage);
        when(userProgressRepository.findByAccount_IdAndTopic_Id(eq(accountId), eq(topicId)))
                .thenReturn(Optional.of(progress));

        Page<TopicProgressResponse> result = topicService.getActiveTopicsProgress(accountId, languageId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Basics");
        assertThat(result.getContent().get(0).getProgressPercent()).isEqualTo(45.0);
        assertThat(result.getContent().get(0).getLastStudiedAt()).isEqualTo(studiedAt);
        verify(topicRepository).findActiveTopicsWithProgress(eq(languageId), eq(accountId), eq(pageable));
        verify(userProgressRepository).findByAccount_IdAndTopic_Id(eq(accountId), eq(topicId));
    }

    @Test
    @DisplayName("Should search topics with name filter only")
    void shouldSearchTopicsWithNameFilterOnly() {
        TopicResponse response = TopicResponse.builder().id(topicId).name("Basics").build();
        when(topicRepository.findAll(any(Specification.class))).thenReturn(List.of(topic));
        when(topicMapper.mapTopicEntitiesToTopicResponse(topic)).thenReturn(response);

        List<TopicResponse> result = topicService.searchTopics("Basics", null, null);

        assertThat(result).hasSize(1);
        verify(topicRepository).findAll(any(Specification.class));
    }
    @Test
    @DisplayName("Should complete exam successfully with valid answers")
    void shouldCompleteExamSuccessfully() {
        UserProgress progress = UserProgress.builder()
                .account(Accounts.builder().id(accountId).build())
                .topic(Topic.builder().id(topicId).build())
                .correctAnswers(0)
                .totalAnswers(0)
                .totalXP(100L)
                .examPassed(false)
                .build();

        FlashcardEntity flashcard = new FlashcardEntity();
        flashcard.setId(UUID.randomUUID());
        flashcard.setBack("Apple");

        QcmQuestionEntity qcm = new QcmQuestionEntity();
        qcm.setId(UUID.randomUUID());
        qcm.setCorrectOptionIndex(1);

        ExamResultRequest request = ExamResultRequest.builder()
                .flashcardAnswers(List.of(new FlashcardAnswerRequest(flashcard.getId(), "Apple")))
                .qcmAnswers(List.of(new QcmAnswerRequest(qcm.getId(), 1)))
                .build();

        when(userProgressRepository.findByAccount_IdAndTopic_Id(any(UUID.class), any(UUID.class))).thenReturn(Optional.of(progress));
        when(flashcardRepository.findById(flashcard.getId())).thenReturn(Optional.of(flashcard));
        when(qcmQuestionRepository.findById(qcm.getId())).thenReturn(Optional.of(qcm));
        when(userProgressRepository.save(any())).thenReturn(progress);

        CompleteExamResponse result = topicService.completeTopicExam(accountId, topicId, request);

        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getXpEarned()).isEqualTo(50);
        assertThat(result.getTotalXP()).isEqualTo(150L);
        assertThat(result.getCurrentLevel()).isEqualTo(2);
        assertThat(result.getLeveledUp()).isFalse();
        assertThat(result.getNewLevel()).isEqualTo(2);
        assertThat(result.getTotalAnswers()).isEqualTo(2);
        assertThat(result.getCorrectAnswers()).isEqualTo(2);
        assertThat(result.getAccuracy()).isEqualTo(1.0);
        assertThat(progress.getExamPassed()).isTrue();
        assertThat(progress.getTotalXP()).isEqualTo(150L);
        assertThat(progress.getCorrectAnswers()).isEqualTo(2);
        assertThat(progress.getTotalAnswers()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should complete exam and level up")
    void shouldCompleteExamAndLevelUp() {
        UserProgress progress = UserProgress.builder()
                .account(Accounts.builder().id(accountId).build())
                .topic(Topic.builder().id(topicId).build())
                .correctAnswers(0)
                .totalAnswers(0)
                .totalXP(980L)
                .examPassed(false)
                .build();

        FlashcardEntity flashcard = new FlashcardEntity();
        flashcard.setId(UUID.randomUUID());
        flashcard.setBack("Apple");

        ExamResultRequest request = ExamResultRequest.builder()
                .flashcardAnswers(List.of(new FlashcardAnswerRequest(flashcard.getId(), "Apple")))
                .build();

        when(userProgressRepository.findByAccount_IdAndTopic_Id(any(UUID.class), any(UUID.class))).thenReturn(Optional.of(progress));
        when(flashcardRepository.findById(flashcard.getId())).thenReturn(Optional.of(flashcard));
        when(userProgressRepository.save(any())).thenReturn(progress);

        CompleteExamResponse result = topicService.completeTopicExam(accountId, topicId, request);

        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getXpEarned()).isEqualTo(50);
        assertThat(result.getTotalXP()).isEqualTo(1030L);
        assertThat(result.getCurrentLevel()).isEqualTo(4);
        assertThat(result.getLeveledUp()).isTrue();
        assertThat(result.getNewLevel()).isEqualTo(5);
        assertThat(progress.getTotalXP()).isEqualTo(1030L);
    }

    @Test
    @DisplayName("Should fail exam when answers are incorrect")
    void shouldFailExamWithIncorrectAnswers() {
        UserProgress progress = UserProgress.builder()
                .correctAnswers(0)
                .totalAnswers(0)
                .totalXP(0L)
                .examPassed(false)
                .build();

        FlashcardEntity flashcard = new FlashcardEntity();
        flashcard.setId(UUID.randomUUID());
        flashcard.setBack("Apple");

        ExamResultRequest request = ExamResultRequest.builder()
                .flashcardAnswers(List.of(new FlashcardAnswerRequest(flashcard.getId(), "Banana")))
                .build();

        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId)).thenReturn(Optional.of(progress));
        when(flashcardRepository.findById(flashcard.getId())).thenReturn(Optional.of(flashcard));
        when(userProgressRepository.save(any())).thenReturn(progress);

        CompleteExamResponse result = topicService.completeTopicExam(accountId, topicId, request);

        assertThat(result).isNotNull();
        assertThat(result.getSuccess()).isNotNull();
        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getXpEarned()).isEqualTo(0);
        assertThat(progress.getExamPassed()).isFalse();
    }

    @Test
    @DisplayName("Should validate flashcard with level 2 (numbers matching)")
    void shouldValidateFlashcardWithNumbers() {
        UserProgress progress = new UserProgress();
        progress.setCorrectAnswers(0);
        progress.setTotalAnswers(0);
        progress.setTotalXP(0L);
        
        FlashcardEntity flashcard = new FlashcardEntity();
        flashcard.setId(UUID.randomUUID());
        flashcard.setBack("100 kilometers");

        ExamResultRequest request = ExamResultRequest.builder()
                .flashcardAnswers(List.of(new FlashcardAnswerRequest(flashcard.getId(), "100 kilomete")))
                .build();

        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId)).thenReturn(Optional.of(progress));
        when(flashcardRepository.findById(flashcard.getId())).thenReturn(Optional.of(flashcard));
        when(userProgressRepository.save(any())).thenReturn(progress);

        CompleteExamResponse result = topicService.completeTopicExam(accountId, topicId, request);

        assertThat(result.getSuccess()).isTrue();
        assertThat(progress.getCorrectAnswers()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should validate interactive question answers during exam")
    void shouldValidateInteractiveQuestionAnswersDuringExam() {
        UserProgress progress = new UserProgress();
        progress.setCorrectAnswers(0);
        progress.setTotalAnswers(0);
        progress.setTotalXP(0L);

        InteractiveQuestionEntity qcmQuestion = new InteractiveQuestionEntity();
        qcmQuestion.setId(UUID.randomUUID());
        qcmQuestion.setSystemType(InteractiveSystemType.MULTIPLE_CHOICE);
        qcmQuestion.setCorrectOptionIndex(2);

        InteractiveQuestionEntity openTextQuestion = new InteractiveQuestionEntity();
        openTextQuestion.setId(UUID.randomUUID());
        openTextQuestion.setSystemType(InteractiveSystemType.OPEN_TEXT);
        openTextQuestion.setCorrectWord("House");

        ExamResultRequest request = ExamResultRequest.builder()
                .interactiveAnswers(List.of(
                        new InteractiveAnswerRequest(qcmQuestion.getId(), 2, null),
                        new InteractiveAnswerRequest(openTextQuestion.getId(), null, "hous")
                ))
                .build();

        when(userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId)).thenReturn(Optional.of(progress));
        when(interactiveQuestionRepository.findById(qcmQuestion.getId())).thenReturn(Optional.of(qcmQuestion));
        when(interactiveQuestionRepository.findById(openTextQuestion.getId())).thenReturn(Optional.of(openTextQuestion));
        when(userProgressRepository.save(any())).thenReturn(progress);

        CompleteExamResponse result = topicService.completeTopicExam(accountId, topicId, request);

        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getCorrectAnswers()).isEqualTo(2);
        assertThat(progress.getCorrectAnswers()).isEqualTo(2);
    }
}
