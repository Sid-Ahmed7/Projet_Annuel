package com.glotrush.mapping;

import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.LessonSummaryResponse;
import com.glotrush.dto.response.exercice.FlashcardExamResponse;
import com.glotrush.dto.response.exercice.QcmQuestionExamResponse;
import com.glotrush.dto.response.exercice.SortingExerciseExamResponse;
import com.glotrush.dto.response.lesson.FlashcardLessonResponse;
import com.glotrush.dto.response.lesson.MatchingPairLessonResponse;
import com.glotrush.dto.response.lesson.QcmLessonResponse;
import com.glotrush.dto.response.lesson.SortingExerciseLessonResponse;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.exercice.FlashcardEntity;
import com.glotrush.entities.exercice.QcmQuestionEntity;
import com.glotrush.entities.exercice.SortingExerciseEntity;
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

    @Mapping(source = "isAlreadyFinish", target = "isAlreadyFinish")
    public abstract LessonSummaryResponse lessonToLessonSummaryResponse(Lesson lesson, Boolean isAlreadyFinish);

    // ALL MAPPING TYPE OF LESSON
    @Mapping(source = "topic.id", target = "topicId")
    @Mapping(source = "topic.name", target = "topicName")
    @Mapping(source = "flashcards", target = "flashcards")
    protected abstract FlashcardLessonResponse mapFlashcardLessonEntityToFlashcardLessonResponse(FlashcardLesson request);

    @Mapping(source = "topic.id", target = "topicId")
    @Mapping(source = "topic.name", target = "topicName")
    @Mapping(source = "matchingPairs", target = "matchingPairs")
    protected abstract MatchingPairLessonResponse mapMatchingPairLessonEntityToMatchingPairLessonResponse(MatchingPairLesson request);

    @Mapping(source = "topic.id", target = "topicId")
    @Mapping(source = "topic.name", target = "topicName")
    @Mapping(source = "questions", target = "questions")
    protected abstract QcmLessonResponse mapQcmLessonEntityToQcmLessonResponse(QcmLesson request);

    @Mapping(source = "topic.id", target = "topicId")
    @Mapping(source = "topic.name", target = "topicName")
    @Mapping(source = "sortingExercise", target = "sortingExercise")
    protected abstract SortingExerciseLessonResponse mapSortingExerciseLessonEntityToSortingExerciseLessonResponse(SortingExerciseLesson request);

    // EXAM SPECIFIC MAPPINGS
    public abstract QcmQuestionExamResponse mapQcmQuestionEntityToQcmQuestionExamResponse(QcmQuestionEntity entity);

    public abstract FlashcardExamResponse mapFlashcardEntityToFlashcardExamResponse(FlashcardEntity entity);

    @Mapping(target = "items", source = "items")
    public abstract SortingExerciseExamResponse mapSortingExerciseEntityToSortingExerciseExamResponse(SortingExerciseEntity entity);
}
