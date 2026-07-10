package com.glotrush.mapping;

import com.glotrush.dto.request.exercice.QcmQuestionRequest;
import com.glotrush.dto.request.lesson.QcmLessonRequest;
import com.glotrush.dto.request.lesson.InteractiveLessonRequest;
import com.glotrush.dto.request.exercice.InteractiveQuestionRequest;
import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.lesson.FlashcardLessonResponse;
import com.glotrush.dto.response.lesson.InteractiveLessonResponse;
import com.glotrush.entities.Topic;
import com.glotrush.entities.exercice.FlashcardEntity;
import com.glotrush.entities.exercice.InteractiveQuestionEntity;
import com.glotrush.entities.lesson.FlashcardLesson;
import com.glotrush.entities.lesson.QcmLesson;
import com.glotrush.entities.lesson.InteractiveLesson;
import com.glotrush.enumerations.LessonType;
import com.glotrush.enumerations.InteractiveSystemType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Lesson Mappers Unit Tests")
class LessonMapperTest {

    @Autowired
    private LessonEntityToLessonResponse entityToResponseMapper;

    @Autowired
    private LessonRequestToLessonEntity requestToEntityMapper;

    @Autowired
    private MessageSource messageSource;

    @Test
    @DisplayName("Should map FlashcardLesson entity to FlashcardLessonResponse with flashcards")
    void shouldMapFlashcardLessonToResponse() {
        UUID lessonId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();
        Topic topic = Topic.builder()
                .id(topicId)
                .name("Topic Name")
                .build();

        FlashcardEntity flashcard = new FlashcardEntity();
        flashcard.setId(UUID.randomUUID());
        flashcard.setFront("Front");
        flashcard.setBack("Back");
        flashcard.setFrontLanguage("fr");
        flashcard.setBackLanguage("en");

        FlashcardLesson entity = FlashcardLesson.builder()
                .id(lessonId)
                .topic(topic)
                .title("Lesson Title")
                .description("Lesson Description")
                .orderIndex(1)
                .xpReward(20)
                .flashcards(List.of(flashcard))
                .isActive(true)
                .build();

        LessonResponse response = entityToResponseMapper.lessonEntityToLessonResponse(entity, messageSource);

        assertThat(response).isInstanceOf(FlashcardLessonResponse.class);
        FlashcardLessonResponse flashcardResponse = (FlashcardLessonResponse) response;
        assertThat(flashcardResponse.getId()).isEqualTo(lessonId);
        assertThat(flashcardResponse.getTopicId()).isEqualTo(topicId);
        assertThat(flashcardResponse.getTopicName()).isEqualTo("Topic Name");
        assertThat(flashcardResponse.getTitle()).isEqualTo(entity.getTitle());
        assertThat(flashcardResponse.getFlashcards()).hasSize(1);
        assertThat(flashcardResponse.getFlashcards().get(0).getFront()).isEqualTo("Front");
    }

    @Test
    @DisplayName("Should map QcmLessonRequest to QcmLesson entity with questions linked")
    void shouldMapQcmLessonRequestToEntity() {
        UUID topicId = UUID.randomUUID();
        QcmLessonRequest request = new QcmLessonRequest();
        request.setTopicId(topicId);
        request.setTitle("QCM Title");
        request.setDescription("QCM Description");
        request.setIsActive(true);
        request.setLessonType(LessonType.QCM);

        QcmQuestionRequest questionRequest = new QcmQuestionRequest();
        questionRequest.setQuestion("What is Java?");
        questionRequest.setOptions(List.of("Language", "Coffee", "Island"));
        questionRequest.setCorrectOptionIndex(0);
        request.setQuestions(List.of(questionRequest));

        QcmLesson entity = (QcmLesson) requestToEntityMapper.lessonRequestToLessonEntity(request, messageSource);

        assertThat(entity).isNotNull();
        assertThat(entity.getTitle()).isEqualTo(request.getTitle());
        assertThat(entity.getQuestions()).hasSize(1);
        assertThat(entity.getQuestions().get(0).getQuestion()).isEqualTo("What is Java?");
        assertThat(entity.getQuestions().get(0).getLesson()).isEqualTo(entity);
    }

    @Test
    @DisplayName("Should map InteractiveLesson entity to InteractiveLessonResponse")
    void shouldMapInteractiveLessonToResponse() {
        UUID lessonId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();
        Topic topic = Topic.builder()
                .id(topicId)
                .name("Interactive Topic")
                .build();

        InteractiveQuestionEntity question = new InteractiveQuestionEntity();
        question.setId(UUID.randomUUID());
        question.setQuestionText("Listen and match");
        question.setImagePaths(List.of("img1.png"));
        question.setAudioPaths(List.of("audio1.mp3"));
        question.setSystemType(InteractiveSystemType.MULTIPLE_CHOICE);
        question.setOptions(List.of("Option A", "Option B"));
        question.setCorrectOptionIndex(0);

        InteractiveLesson entity = InteractiveLesson.builder()
                .id(lessonId)
                .topic(topic)
                .title("Interactive Title")
                .description("Interactive Desc")
                .orderIndex(1)
                .xpReward(15)
                .questions(List.of(question))
                .isActive(true)
                .build();

        LessonResponse response = entityToResponseMapper.lessonEntityToLessonResponse(entity, messageSource);

        assertThat(response).isInstanceOf(InteractiveLessonResponse.class);
        InteractiveLessonResponse interactiveResponse = (InteractiveLessonResponse) response;
        assertThat(interactiveResponse.getId()).isEqualTo(lessonId);
        assertThat(interactiveResponse.getTopicId()).isEqualTo(topicId);
        assertThat(interactiveResponse.getQuestions()).hasSize(1);
        assertThat(interactiveResponse.getQuestions().get(0).getQuestionText()).isEqualTo("Listen and match");
        assertThat(interactiveResponse.getQuestions().get(0).getImagePaths()).containsExactly("img1.png");
    }

    @Test
    @DisplayName("Should map InteractiveLessonRequest to InteractiveLesson entity")
    void shouldMapInteractiveLessonRequestToEntity() {
        UUID topicId = UUID.randomUUID();
        InteractiveLessonRequest request = new InteractiveLessonRequest();
        request.setTopicId(topicId);
        request.setTitle("Interactive Request");
        request.setDescription("Interactive Request Desc");
        request.setIsActive(true);
        request.setLessonType(LessonType.INTERACTIVE);

        InteractiveQuestionRequest questionRequest = new InteractiveQuestionRequest();
        questionRequest.setQuestionText("Write what you hear");
        questionRequest.setSystemType(InteractiveSystemType.OPEN_TEXT);
        questionRequest.setCorrectWord("hello");
        questionRequest.setImagePaths(List.of("hello.png"));
        request.setInteractiveQuestions(List.of(questionRequest));

        InteractiveLesson entity = (InteractiveLesson) requestToEntityMapper.lessonRequestToLessonEntity(request, messageSource);

        assertThat(entity).isNotNull();
        assertThat(entity.getTitle()).isEqualTo(request.getTitle());
        assertThat(entity.getQuestions()).hasSize(1);
        assertThat(entity.getQuestions().get(0).getQuestionText()).isEqualTo("Write what you hear");
        assertThat(entity.getQuestions().get(0).getCorrectWord()).isEqualTo("hello");
        assertThat(entity.getQuestions().get(0).getLesson()).isEqualTo(entity);
    }
}
