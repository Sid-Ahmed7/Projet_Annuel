package com.glotrush.mapping;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.request.lesson.FlashcardLessonRequest;
import com.glotrush.dto.request.lesson.MatchingPairLessonRequest;
import com.glotrush.dto.request.lesson.QcmLessonRequest;
import com.glotrush.dto.request.lesson.SortingExerciseLessonRequest;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.lesson.FlashcardLesson;
import com.glotrush.entities.lesson.MatchingPairLesson;
import com.glotrush.entities.lesson.QcmLesson;
import com.glotrush.entities.lesson.SortingExerciseLesson;
import com.glotrush.exceptions.LessonNotFoundException;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

@Mapper(componentModel = "spring")
public abstract class LessonRequestToLessonEntity {

    protected final Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    public Lesson lessonRequestToLessonEntity(LessonRequest request, @Context MessageSource messageSource) {
        return switch (request){
            case FlashcardLessonRequest flashcardLessonRequest -> mapFlashcardLessonRequestToFlashcardLessonEntity(flashcardLessonRequest);
            case MatchingPairLessonRequest matchingPairLessonRequest -> mapMatchingPairLessonRequestToMatchingPairLessonEntity(matchingPairLessonRequest);
            case QcmLessonRequest qcmLessonRequest -> mapQcmLessonRequestToQcmLessonEntity(qcmLessonRequest);
            case SortingExerciseLessonRequest sortingExerciseLessonRequest -> mapSortingExerciseLessonRequestToSortingExerciseLessonEntity(sortingExerciseLessonRequest);
            case null, default -> throw new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, getCurrentLocale()));
        };
    }

    // ALL MAPPING TYPE OF LESSON
    protected abstract FlashcardLesson mapFlashcardLessonRequestToFlashcardLessonEntity(FlashcardLessonRequest request);

    protected abstract MatchingPairLesson mapMatchingPairLessonRequestToMatchingPairLessonEntity(MatchingPairLessonRequest request);

    protected abstract QcmLesson mapQcmLessonRequestToQcmLessonEntity(QcmLessonRequest request);

    protected abstract SortingExerciseLesson mapSortingExerciseLessonRequestToSortingExerciseLessonEntity(SortingExerciseLessonRequest request);
}
