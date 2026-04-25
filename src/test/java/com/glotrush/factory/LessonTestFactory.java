package com.glotrush.factory;
import com.glotrush.dto.request.lesson.FlashcardLessonRequest;
import com.glotrush.dto.request.lesson.MatchingPairLessonRequest;
import com.glotrush.dto.request.lesson.QcmLessonRequest;
import com.glotrush.dto.request.lesson.SortingExerciseLessonRequest;
import com.glotrush.dto.response.lesson.FlashcardLessonResponse;
import com.glotrush.dto.response.lesson.MatchingPairLessonResponse;
import com.glotrush.dto.response.lesson.QcmLessonResponse;
import com.glotrush.dto.response.lesson.SortingExerciseLessonResponse;
import com.glotrush.entities.Topic;
import com.glotrush.entities.lesson.FlashcardLesson;
import com.glotrush.entities.lesson.MatchingPairLesson;
import com.glotrush.entities.lesson.QcmLesson;
import com.glotrush.entities.lesson.SortingExerciseLesson;
import com.glotrush.enumerations.LessonType;
import java.util.ArrayList;
import java.util.UUID;
public class LessonTestFactory {
    public static Topic createTopic(UUID id, String name) {
        return Topic.builder()
                .id(id)
                .name(name)
                .targetLanguage(com.glotrush.entities.Language.builder().id(UUID.randomUUID()).name("English").build())
                .sourceLanguage(com.glotrush.entities.Language.builder().id(UUID.randomUUID()).name("French").build())
                .build();
    }
    public static FlashcardLessonRequest createFlashcardLessonRequest(UUID topicId, String title) {
        FlashcardLessonRequest request = new FlashcardLessonRequest();
        request.setTopicId(topicId);
        request.setTitle(title);
        request.setDescription("Description");
        request.setIsActive(true);
        request.setFlashcards(new ArrayList<>());
        request.setLessonType(LessonType.FLASHCARD);
        return request;
    }
    public static FlashcardLesson createFlashcardLesson(UUID id, Topic topic, String title) {
        return FlashcardLesson.builder()
                .id(id)
                .topic(topic)
                .title(title)
                .description("Description")
                .orderIndex(1)
                .xpReward(50)
                .durationMinutes(15)
                .isActive(true)
                .flashcards(new ArrayList<>())
                .build();
    }
    public static FlashcardLessonResponse createFlashcardLessonResponse(UUID id, String title) {
        return FlashcardLessonResponse.builder()
                .id(id)
                .title(title)
                .description("Description")
                .orderIndex(1)
                .xpReward(50)
                .durationMinutes(15)
                .isActive(true)
                .flashcards(new ArrayList<>())
                .lessonType(LessonType.FLASHCARD)
                .build();
    }
    public static MatchingPairLesson createMatchingPairLesson(UUID id, Topic topic, String title) {
        return MatchingPairLesson.builder()
                .id(id)
                .topic(topic)
                .title(title)
                .description("Description")
                .orderIndex(1)
                .xpReward(50)
                .durationMinutes(15)
                .isActive(true)
                .matchingPairs(new ArrayList<>())
                .build();
    }
    public static MatchingPairLessonResponse createMatchingPairLessonResponse(UUID id, String title) {
        return MatchingPairLessonResponse.builder()
                .id(id)
                .title(title)
                .description("Description")
                .orderIndex(1)
                .xpReward(50)
                .durationMinutes(15)
                .isActive(true)
                .matchingPairs(new ArrayList<>())
                .lessonType(LessonType.MATCHING_PAIR)
                .build();
    }
    public static QcmLesson createQcmLesson(UUID id, Topic topic, String title) {
        return QcmLesson.builder()
                .id(id)
                .topic(topic)
                .title(title)
                .description("Description")
                .orderIndex(1)
                .xpReward(50)
                .durationMinutes(15)
                .isActive(true)
                .questions(new ArrayList<>())
                .build();
    }
    public static QcmLessonResponse createQcmLessonResponse(UUID id, String title) {
        return QcmLessonResponse.builder()
                .id(id)
                .title(title)
                .description("Description")
                .orderIndex(1)
                .xpReward(50)
                .durationMinutes(15)
                .isActive(true)
                .questions(new ArrayList<>())
                .lessonType(LessonType.QCM)
                .build();
    }
    public static SortingExerciseLesson createSortingExerciseLesson(UUID id, Topic topic, String title) {
        return SortingExerciseLesson.builder()
                .id(id)
                .topic(topic)
                .title(title)
                .description("Description")
                .orderIndex(1)
                .xpReward(50)
                .durationMinutes(15)
                .isActive(true)
                .sortingExercise(new ArrayList<>())
                .build();
    }
    public static SortingExerciseLessonResponse createSortingExerciseLessonResponse(UUID id, String title) {
        return SortingExerciseLessonResponse.builder()
                .id(id)
                .title(title)
                .description("Description")
                .orderIndex(1)
                .xpReward(50)
                .durationMinutes(15)
                .isActive(true)
                .sortingExercise(new ArrayList<>())
                .lessonType(LessonType.SORTING_EXERCISE)
                .build();
    }
    public static QcmLessonRequest createQcmLessonRequest(UUID topicId, String title) {
        QcmLessonRequest request = new QcmLessonRequest();
        request.setTopicId(topicId);
        request.setTitle(title);
        request.setDescription("Description");
        request.setIsActive(true);
        request.setQuestions(new ArrayList<>());
        request.setLessonType(LessonType.QCM);
        return request;
    }
    public static MatchingPairLessonRequest createMatchingPairLessonRequest(UUID topicId, String title) {
        MatchingPairLessonRequest request = new MatchingPairLessonRequest();
        request.setTopicId(topicId);
        request.setTitle(title);
        request.setDescription("Description");
        request.setIsActive(true);
        request.setMatchingPairs(new ArrayList<>());
        request.setLessonType(LessonType.MATCHING_PAIR);
        return request;
    }
    public static SortingExerciseLessonRequest createSortingExerciseLessonRequest(UUID topicId, String title) {
        SortingExerciseLessonRequest request = new SortingExerciseLessonRequest();
        request.setTopicId(topicId);
        request.setTitle(title);
        request.setDescription("Description");
        request.setIsActive(true);
        request.setSortingExercise(new ArrayList<>());
        request.setLessonType(LessonType.SORTING_EXERCISE);
        return request;
    }
}
