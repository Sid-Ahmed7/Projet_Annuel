package com.glotrush.services.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.request.exercice.FlashcardRequest;
import com.glotrush.dto.request.exercice.MatchingPairRequest;
import com.glotrush.dto.request.exercice.QcmQuestionRequest;
import com.glotrush.dto.request.exercice.SortingExerciseRequest;
import com.glotrush.dto.request.exercice.InteractiveQuestionRequest;
import com.glotrush.dto.request.lesson.FlashcardLessonRequest;
import com.glotrush.dto.request.lesson.MatchingPairLessonRequest;
import com.glotrush.dto.request.lesson.QcmLessonRequest;
import com.glotrush.dto.request.lesson.SortingExerciseLessonRequest;
import com.glotrush.dto.request.lesson.InteractiveLessonRequest;
import com.glotrush.entities.Language;
import com.glotrush.entities.Topic;
import com.glotrush.enumerations.LessonType;
import com.glotrush.repositories.LanguageRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.ai.AIGenerationLogRepository;
import com.glotrush.mapping.LessonEntityToLessonResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AILessonGeneratorService Unit Tests")
class AILessonGeneratorServiceTest {

    @Mock
    private IAIService aiService;

    @Mock
    private AIGenerationLogRepository logRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private LessonEntityToLessonResponse lessonEntityToLessonResponse;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AIQuotaService aiQuotaService;

    private IAILessonGeneratorService aiLessonGeneratorService;

    private UUID accountId;
    private UUID topicId;
    private Topic testTopic;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        topicId = UUID.randomUUID();
        
        aiLessonGeneratorService = new AILessonGeneratorService(
                aiService,
                logRepository,
                topicRepository,
                lessonRepository,
                languageRepository,
                lessonEntityToLessonResponse,
                messageSource,
                objectMapper,
                aiQuotaService
        );

        Language french = Language.builder().id(UUID.randomUUID()).name("French").build();
        Language english = Language.builder().id(UUID.randomUUID()).name("English").build();

        testTopic = Topic.builder()
                .id(topicId)
                .name("Test Topic")
                .sourceLanguage(french)
                .targetLanguage(english)
                .build();
    }

    @Test
    @DisplayName("Should truncate matching pairs to requested itemCount when AI generates more")
    void shouldTruncateMatchingPairsToRequestedItemCountWhenAiGeneratesMore() {
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));

        MatchingPairLessonRequest aiResponse = new MatchingPairLessonRequest();
        List<MatchingPairRequest> pairs = new ArrayList<>();
        
        for (int index = 0; index < 5; index++) {
            MatchingPairRequest pair = new MatchingPairRequest();
            pair.setItem1("Word " + index);
            pair.setItem2("Mot " + index);
            pairs.add(pair);
        }
        
        aiResponse.setMatchingPairs(pairs);

        when(aiService.generateJsonContent(any(String.class), eq(MatchingPairLessonRequest.class)))
                .thenReturn(aiResponse);

        LessonRequest result = aiLessonGeneratorService.generateLesson(
                accountId,
                topicId,
                LessonType.MATCHING_PAIR,
                "Test description",
                3
        );

        assertThat(result).isInstanceOf(MatchingPairLessonRequest.class);
        
        MatchingPairLessonRequest truncatedResult = (MatchingPairLessonRequest) result;
        
        assertThat(truncatedResult.getMatchingPairs()).hasSize(3);
        assertThat(truncatedResult.getMatchingPairs().get(0).getItem1()).isEqualTo("Word 0");
        assertThat(truncatedResult.getMatchingPairs().get(2).getItem1()).isEqualTo("Word 2");
    }

    @Test
    @DisplayName("Should truncate QCM questions to requested itemCount when AI generates more")
    void shouldTruncateQcmQuestionsToRequestedItemCountWhenAiGeneratesMore() {
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));

        QcmLessonRequest aiResponse = new QcmLessonRequest();
        List<QcmQuestionRequest> questions = new ArrayList<>();
        
        for (int index = 0; index < 8; index++) {
            QcmQuestionRequest question = new QcmQuestionRequest();
            question.setQuestion("Question " + index);
            questions.add(question);
        }
        
        aiResponse.setQuestions(questions);

        when(aiService.generateJsonContent(any(String.class), eq(QcmLessonRequest.class)))
                .thenReturn(aiResponse);

        LessonRequest result = aiLessonGeneratorService.generateLesson(
                accountId,
                topicId,
                LessonType.QCM,
                "Test description",
                5
        );

        assertThat(result).isInstanceOf(QcmLessonRequest.class);
        
        QcmLessonRequest truncatedResult = (QcmLessonRequest) result;
        
        assertThat(truncatedResult.getQuestions()).hasSize(5);
        assertThat(truncatedResult.getQuestions().get(0).getQuestion()).isEqualTo("Question 0");
        assertThat(truncatedResult.getQuestions().get(4).getQuestion()).isEqualTo("Question 4");
    }

    @Test
    @DisplayName("Should truncate flashcards to requested itemCount when AI generates more")
    void shouldTruncateFlashcardsToRequestedItemCountWhenAiGeneratesMore() {
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));

        FlashcardLessonRequest aiResponse = new FlashcardLessonRequest();
        List<FlashcardRequest> flashcards = new ArrayList<>();
        
        for (int index = 0; index < 10; index++) {
            FlashcardRequest flashcard = new FlashcardRequest();
            flashcard.setFront("Front " + index);
            flashcards.add(flashcard);
        }
        
        aiResponse.setFlashcards(flashcards);

        when(aiService.generateJsonContent(any(String.class), eq(FlashcardLessonRequest.class)))
                .thenReturn(aiResponse);

        LessonRequest result = aiLessonGeneratorService.generateLesson(
                accountId,
                topicId,
                LessonType.FLASHCARD,
                "Test description",
                6
        );

        assertThat(result).isInstanceOf(FlashcardLessonRequest.class);
        
        FlashcardLessonRequest truncatedResult = (FlashcardLessonRequest) result;
        
        assertThat(truncatedResult.getFlashcards()).hasSize(6);
        assertThat(truncatedResult.getFlashcards().get(0).getFront()).isEqualTo("Front 0");
        assertThat(truncatedResult.getFlashcards().get(5).getFront()).isEqualTo("Front 5");
    }

    @Test
    @DisplayName("Should truncate sorting exercises to requested itemCount when AI generates more")
    void shouldTruncateSortingExercisesToRequestedItemCountWhenAiGeneratesMore() {
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));

        SortingExerciseLessonRequest aiResponse = new SortingExerciseLessonRequest();
        List<SortingExerciseRequest> sortingExercises = new ArrayList<>();
        
        for (int index = 0; index < 6; index++) {
            SortingExerciseRequest exercise = new SortingExerciseRequest();
            exercise.setItems(List.of("A", "B", "C"));
            sortingExercises.add(exercise);
        }
        
        aiResponse.setSortingExercise(sortingExercises);

        when(aiService.generateJsonContent(any(String.class), eq(SortingExerciseLessonRequest.class)))
                .thenReturn(aiResponse);

        LessonRequest result = aiLessonGeneratorService.generateLesson(
                accountId,
                topicId,
                LessonType.SORTING_EXERCISE,
                "Test description",
                4
        );

        assertThat(result).isInstanceOf(SortingExerciseLessonRequest.class);
        
        SortingExerciseLessonRequest truncatedResult = (SortingExerciseLessonRequest) result;
        
        assertThat(truncatedResult.getSortingExercise()).hasSize(4);
    }

    @Test
    @DisplayName("Should not truncate items when AI generates fewer than requested itemCount")
    void shouldNotTruncateWhenAiGeneratesFewerThanRequestedItemCount() {
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));

        MatchingPairLessonRequest aiResponse = new MatchingPairLessonRequest();
        List<MatchingPairRequest> pairs = new ArrayList<>();
        
        for (int index = 0; index < 3; index++) {
            MatchingPairRequest pair = new MatchingPairRequest();
            pair.setItem1("Word " + index);
            pairs.add(pair);
        }
        
        aiResponse.setMatchingPairs(pairs);

        when(aiService.generateJsonContent(any(String.class), eq(MatchingPairLessonRequest.class)))
                .thenReturn(aiResponse);

        LessonRequest result = aiLessonGeneratorService.generateLesson(
                accountId,
                topicId,
                LessonType.MATCHING_PAIR,
                "Test description",
                5
        );

        assertThat(result).isInstanceOf(MatchingPairLessonRequest.class);
        
        MatchingPairLessonRequest truncatedResult = (MatchingPairLessonRequest) result;
        
        assertThat(truncatedResult.getMatchingPairs()).hasSize(3);
    }

    @Test
    @DisplayName("Should not truncate items when itemCount is null")
    void shouldNotTruncateWhenItemCountIsNull() {
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));

        MatchingPairLessonRequest aiResponse = new MatchingPairLessonRequest();
        List<MatchingPairRequest> pairs = new ArrayList<>();
        
        for (int index = 0; index < 5; index++) {
            MatchingPairRequest pair = new MatchingPairRequest();
            pair.setItem1("Word " + index);
            pairs.add(pair);
        }
        
        aiResponse.setMatchingPairs(pairs);

        when(aiService.generateJsonContent(any(String.class), eq(MatchingPairLessonRequest.class)))
                .thenReturn(aiResponse);

        LessonRequest result = aiLessonGeneratorService.generateLesson(
                accountId,
                topicId,
                LessonType.MATCHING_PAIR,
                "Test description",
                null
        );

        assertThat(result).isInstanceOf(MatchingPairLessonRequest.class);
        
        MatchingPairLessonRequest truncatedResult = (MatchingPairLessonRequest) result;
        
        assertThat(truncatedResult.getMatchingPairs()).hasSize(5);
    }

    @Test
    @DisplayName("Should generate interactive lesson with empty media lists and respect itemCount")
    void shouldGenerateInteractiveLessonWithEmptyMediaListsAndRespectItemCount() {
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));

        InteractiveLessonRequest aiResponse = new InteractiveLessonRequest();
        List<InteractiveQuestionRequest> questions = new ArrayList<>();
        
        for (int index = 0; index < 8; index++) {
            InteractiveQuestionRequest question = new InteractiveQuestionRequest();
            question.setQuestionText("Question " + index);
            question.setImagePaths(List.of("mock_image.jpg"));
            question.setAudioPaths(List.of("mock_audio.mp3"));
            questions.add(question);
        }
        
        aiResponse.setInteractiveQuestions(questions);

        when(aiService.generateJsonContent(any(String.class), eq(InteractiveLessonRequest.class)))
                .thenReturn(aiResponse);

        LessonRequest result = aiLessonGeneratorService.generateLesson(
                accountId,
                topicId,
                LessonType.INTERACTIVE,
                "Test description",
                5
        );

        assertThat(result).isInstanceOf(InteractiveLessonRequest.class);
        
        InteractiveLessonRequest interactiveResult = (InteractiveLessonRequest) result;
        
        assertThat(interactiveResult.getInteractiveQuestions()).hasSize(5);
        for (InteractiveQuestionRequest question : interactiveResult.getInteractiveQuestions()) {
            assertThat(question.getImagePaths()).isEmpty();
            assertThat(question.getAudioPaths()).isEmpty();
        }
    }

    @Test
    @DisplayName("Should generate challenge content when target and source languages exist")
    void shouldGenerateChallengeContentWhenLanguagesExist() {
        UUID sourceLanguageId = UUID.randomUUID();
        UUID targetLanguageId = UUID.randomUUID();
        Language sourceLanguage = Language.builder().id(sourceLanguageId).name("French").build();
        Language targetLanguage = Language.builder().id(targetLanguageId).name("English").build();

        when(languageRepository.findById(sourceLanguageId)).thenReturn(Optional.of(sourceLanguage));
        when(languageRepository.findById(targetLanguageId)).thenReturn(Optional.of(targetLanguage));

        QcmLessonRequest aiResponse = new QcmLessonRequest();
        List<QcmQuestionRequest> questions = new ArrayList<>();
        QcmQuestionRequest question = new QcmQuestionRequest();
        question.setQuestion("Bonjour");
        questions.add(question);
        aiResponse.setQuestions(questions);

        when(aiService.generateJsonContent(any(String.class), eq(QcmLessonRequest.class)))
                .thenReturn(aiResponse);

        LessonRequest result = aiLessonGeneratorService.generateChallengeContent(
                accountId,
                sourceLanguageId,
                targetLanguageId,
                LessonType.QCM,
                "Test description",
                1
        );

        assertThat(result).isInstanceOf(QcmLessonRequest.class);
        assertThat(result.getTopicId()).isNull();
        assertThat(result.getLessonType()).isEqualTo(LessonType.QCM);
    }
}
