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
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

@Mapper(componentModel = "spring")
public abstract class LessonEntityToLessonResponse {
    protected final Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    public LessonResponse lessonEntityToLessonResponse(Lesson entity, @Context MessageSource messageSource) {
        return switch (entity){
            case FlashcardLesson flashcardLesson -> mapFlashcardLessonEntityToFlashcardLessonResponse(flashcardLesson);
            case MatchingPairLesson matchingPairLesson -> mapMatchingPairLessonEntityToMatchingPairLessonResponse(matchingPairLesson);
            case QcmLesson qcmLesson -> mapQcmLessonEntityToQcmLessonResponse(qcmLesson);
            case SortingExerciseLesson sortingExerciseLesson -> mapSortingExerciseLessonEntityToSortingExerciseLessonResponse(sortingExerciseLesson);
            case null, default -> throw new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, getCurrentLocale()));
        };
    }

    // ALL MAPPING TYPE OF LESSON
    @Mapping(target = "id", ignore = true)
    protected abstract FlashcardLessonResponse mapFlashcardLessonEntityToFlashcardLessonResponse(FlashcardLesson request);

    @Mapping(target = "id", ignore = true)
    protected abstract MatchingPairLessonResponse mapMatchingPairLessonEntityToMatchingPairLessonResponse(MatchingPairLesson request);

    @Mapping(target = "id", ignore = true)
    protected abstract QcmLessonResponse mapQcmLessonEntityToQcmLessonResponse(QcmLesson request);

    @Mapping(target = "id", ignore = true)
    protected abstract SortingExerciseLessonResponse mapSortingExerciseLessonEntityToSortingExerciseLessonResponse(SortingExerciseLesson request);
}
