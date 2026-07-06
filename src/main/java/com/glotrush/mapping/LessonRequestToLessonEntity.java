package com.glotrush.mapping;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.request.lesson.FlashcardLessonRequest;
import com.glotrush.dto.request.lesson.MatchingPairLessonRequest;
import com.glotrush.dto.request.lesson.QcmLessonRequest;
import com.glotrush.dto.request.lesson.SortingExerciseLessonRequest;
import com.glotrush.dto.request.lesson.InteractiveLessonRequest;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.lesson.FlashcardLesson;
import com.glotrush.entities.lesson.MatchingPairLesson;
import com.glotrush.entities.lesson.QcmLesson;
import com.glotrush.entities.lesson.SortingExerciseLesson;
import com.glotrush.entities.lesson.InteractiveLesson;
import com.glotrush.exceptions.LessonNotFoundException;
import com.glotrush.utils.LocaleUtils;

import org.mapstruct.*;
import org.springframework.context.MessageSource;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class LessonRequestToLessonEntity {

    @AfterMapping
    protected void linkFlashcards(@MappingTarget FlashcardLesson lesson) {
        if (lesson.getFlashcards() != null) {
            lesson.getFlashcards().forEach(f -> f.setLesson(lesson));
        }
    }

    @AfterMapping
    protected void linkMatchingPairs(@MappingTarget MatchingPairLesson lesson) {
        if (lesson.getMatchingPairs() != null) {
            lesson.getMatchingPairs().forEach(m -> m.setLesson(lesson));
        }
    }

    @AfterMapping
    protected void linkQuestions(@MappingTarget QcmLesson lesson) {
        if (lesson.getQuestions() != null) {
            lesson.getQuestions().forEach(q -> q.setLesson(lesson));
        }
    }

    @AfterMapping
    protected void linkSortingExercises(@MappingTarget SortingExerciseLesson lesson) {
        if (lesson.getSortingExercise() != null) {
            lesson.getSortingExercise().forEach(s -> s.setLesson(lesson));
        }
    }

    @AfterMapping
    protected void linkInteractiveQuestions(@MappingTarget InteractiveLesson lesson) {
        if (lesson.getQuestions() != null) {
            lesson.getQuestions().forEach(q -> q.setLesson(lesson));
        }
    }


    public Lesson lessonRequestToLessonEntity(LessonRequest request, @Context MessageSource messageSource) {
        return switch (request){
            case FlashcardLessonRequest flashcardLessonRequest -> mapFlashcardLessonRequestToFlashcardLessonEntity(flashcardLessonRequest);
            case MatchingPairLessonRequest matchingPairLessonRequest -> mapMatchingPairLessonRequestToMatchingPairLessonEntity(matchingPairLessonRequest);
            case QcmLessonRequest qcmLessonRequest -> mapQcmLessonRequestToQcmLessonEntity(qcmLessonRequest);
            case SortingExerciseLessonRequest sortingExerciseLessonRequest -> mapSortingExerciseLessonRequestToSortingExerciseLessonEntity(sortingExerciseLessonRequest);
            case InteractiveLessonRequest interactiveLessonRequest -> mapInteractiveLessonRequestToInteractiveLessonEntity(interactiveLessonRequest);
            case null, default -> throw new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, LocaleUtils.getCurrentLocale()));
        };
    }

    public void updateLessonFromRequest(LessonRequest request, @MappingTarget Lesson lesson, @Context MessageSource messageSource) {
        switch (request) {
            case FlashcardLessonRequest flashcardLessonRequest -> updateFlashcardLessonFromRequest(flashcardLessonRequest, (FlashcardLesson) lesson);
            case MatchingPairLessonRequest matchingPairLessonRequest -> updateMatchingPairLessonFromRequest(matchingPairLessonRequest, (MatchingPairLesson) lesson);
            case QcmLessonRequest qcmLessonRequest -> updateQcmLessonFromRequest(qcmLessonRequest, (QcmLesson) lesson);
            case SortingExerciseLessonRequest sortingExerciseLessonRequest -> updateSortingExerciseLessonFromRequest(sortingExerciseLessonRequest, (SortingExerciseLesson) lesson);
            case InteractiveLessonRequest interactiveLessonRequest -> updateInteractiveLessonFromRequest(interactiveLessonRequest, (InteractiveLesson) lesson);
            case null, default -> throw new LessonNotFoundException(messageSource.getMessage("error.lesson.notfound", null, LocaleUtils.getCurrentLocale()));
        }
    }

    protected abstract FlashcardLesson mapFlashcardLessonRequestToFlashcardLessonEntity(FlashcardLessonRequest request);

    protected abstract MatchingPairLesson mapMatchingPairLessonRequestToMatchingPairLessonEntity(MatchingPairLessonRequest request);

    protected abstract QcmLesson mapQcmLessonRequestToQcmLessonEntity(QcmLessonRequest request);

    protected abstract SortingExerciseLesson mapSortingExerciseLessonRequestToSortingExerciseLessonEntity(SortingExerciseLessonRequest request);

    protected abstract InteractiveLesson mapInteractiveLessonRequestToInteractiveLessonEntity(InteractiveLessonRequest request);

    protected abstract void updateFlashcardLessonFromRequest(FlashcardLessonRequest request, @MappingTarget FlashcardLesson lesson);

    protected abstract void updateMatchingPairLessonFromRequest(MatchingPairLessonRequest request, @MappingTarget MatchingPairLesson lesson);

    protected abstract void updateQcmLessonFromRequest(QcmLessonRequest request, @MappingTarget QcmLesson lesson);

    protected abstract void updateSortingExerciseLessonFromRequest(SortingExerciseLessonRequest request, @MappingTarget SortingExerciseLesson lesson);

    protected abstract void updateInteractiveLessonFromRequest(InteractiveLessonRequest request, @MappingTarget InteractiveLesson lesson);
}
