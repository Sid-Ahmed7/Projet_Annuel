package com.glotrush.mapping;

import com.glotrush.dto.request.lesson.FlashcardLessonRequest;
import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.lesson.FlashcardLessonResponse;
import com.glotrush.entities.Topic;
import com.glotrush.entities.lesson.FlashcardLesson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;

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
    @DisplayName("Should map FlashcardLesson entity to FlashcardLessonResponse with foreign keys")
    void shouldMapFlashcardLessonToResponse() {
        UUID lessonId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();
        Topic topic = Topic.builder()
                .id(topicId)
                .name("Topic Name")
                .build();

        FlashcardLesson entity = FlashcardLesson.builder()
                .id(lessonId)
                .topic(topic)
                .title("Lesson Title")
                .description("Lesson Description")
                .orderIndex(1)
                .xpReward(20)
                .isLocked(false)
                .isActive(true)
                .build();

        LessonResponse response = entityToResponseMapper.lessonEntityToLessonResponse(entity, messageSource);

        assertThat(response).isInstanceOf(FlashcardLessonResponse.class);
        assertThat(response.getId()).isEqualTo(lessonId);
        assertThat(response.getTopicId()).isEqualTo(topicId);
        assertThat(response.getTopicName()).isEqualTo("Topic Name");
        assertThat(response.getTitle()).isEqualTo(entity.getTitle());
    }

    @Test
    @DisplayName("Should map FlashcardLessonRequest to FlashcardLesson entity")
    void shouldMapFlashcardLessonRequestToEntity() {
        UUID topicId = UUID.randomUUID();
        FlashcardLessonRequest request = new FlashcardLessonRequest();
        request.setTopicId(topicId);
        request.setTitle("Request Title");
        request.setDescription("Request Description");
        request.setOrderIndex(1);
        request.setXpReward(15);
        request.setIsLocked(false);
        request.setIsActive(true);
        request.setMinLevelRequired(1);
        request.setDurationMinutes(10);

        FlashcardLesson entity = (FlashcardLesson) requestToEntityMapper.lessonRequestToLessonEntity(request, messageSource);

        assertThat(entity).isNotNull();
        assertThat(entity.getTitle()).isEqualTo(request.getTitle());
        // topicId n'est pas mappé directement en entité Topic par MapStruct sans configuration
    }
}
