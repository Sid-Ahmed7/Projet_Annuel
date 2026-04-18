package com.glotrush.mapping;

import com.glotrush.dto.request.exercice.FlashcardRequest;
import com.glotrush.dto.request.exercice.QcmQuestionRequest;
import com.glotrush.dto.request.lesson.FlashcardLessonRequest;
import com.glotrush.dto.request.lesson.QcmLessonRequest;
import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.lesson.FlashcardLessonResponse;
import com.glotrush.dto.response.exercice.FlashcardResponse;
import com.glotrush.dto.response.lesson.QcmLessonResponse;
import com.glotrush.entities.Topic;
import com.glotrush.entities.exercice.FlashcardEntity;
import com.glotrush.entities.lesson.FlashcardLesson;
import com.glotrush.entities.lesson.QcmLesson;
import com.glotrush.enumerations.LessonType;
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
        request.setOrderIndex(1);
        request.setXpReward(15);
        request.setIsActive(true);
        request.setMinLevelRequired(1);
        request.setDurationMinutes(10);
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
}
