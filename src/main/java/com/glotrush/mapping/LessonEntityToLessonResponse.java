package com.glotrush.mapping;

import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.lesson.FlashcardLessonResponse;
import com.glotrush.dto.response.lesson.MatchingPairLessonResponse;
import com.glotrush.dto.response.lesson.QcmLessonResponse;
import com.glotrush.dto.response.lesson.SortingExerciseLessonResponse;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.lesson.FlashcardLesson;
import com.glotrush.entities.lesson.MatchingPairLesson;
import com.glotrush.entities.lesson.QcmLesson;
import com.glotrush.entities.lesson.SortingExerciseLesson;
import com.glotrush.exceptions.LessonNotFoundException;
import com.glotrush.utils.LocaleUtils;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.context.MessageSource;


@Mapper(componentModel = "spring")
public abstract class LessonEntityToLessonResponse {

    public LessonResponse lessonEntityToLessonResponse(Lesson entity, @Context MessageSource messageSource) {
        if (entity == null) {
            throw new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, LocaleUtils.getCurrentLocale()));
        }

        return switch (entity) {
            case FlashcardLesson flashcardLesson -> mapFlashcardLessonEntityToFlashcardLessonResponse(flashcardLesson);
            case MatchingPairLesson matchingPairLesson -> mapMatchingPairLessonEntityToMatchingPairLessonResponse(matchingPairLesson);
            case QcmLesson qcmLesson -> mapQcmLessonEntityToQcmLessonResponse(qcmLesson);
            case SortingExerciseLesson sortingExerciseLesson -> mapSortingExerciseLessonEntityToSortingExerciseLessonResponse(sortingExerciseLesson);
            default -> throw new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, LocaleUtils.getCurrentLocale()));
        };
    }

    // ALL MAPPING TYPE OF LESSON
    @Mapping(source = "topic.id", target = "topicId")
    @Mapping(source = "topic.name", target = "topicName")
    @Mapping(source = "flashcards", target = "flashcards")
    protected abstract FlashcardLessonResponse mapFlashcardLessonEntityToFlashcardLessonResponse(FlashcardLesson request);

    @Mapping(source = "topic.id", target = "topicId")
    @Mapping(source = "topic.name", target = "topicName")
    @Mapping(source = "matchingPair", target = "matchingPair")
    protected abstract MatchingPairLessonResponse mapMatchingPairLessonEntityToMatchingPairLessonResponse(MatchingPairLesson request);

    @Mapping(source = "topic.id", target = "topicId")
    @Mapping(source = "topic.name", target = "topicName")
    @Mapping(source = "questions", target = "questions")
    protected abstract QcmLessonResponse mapQcmLessonEntityToQcmLessonResponse(QcmLesson request);

    @Mapping(source = "topic.id", target = "topicId")
    @Mapping(source = "topic.name", target = "topicName")
    @Mapping(source = "sortingExercise", target = "sortingExercise")
    protected abstract SortingExerciseLessonResponse mapSortingExerciseLessonEntityToSortingExerciseLessonResponse(SortingExerciseLesson request);
}
