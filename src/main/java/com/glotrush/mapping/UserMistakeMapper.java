package com.glotrush.mapping;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.glotrush.builder.UserMistakeBuilder;
import com.glotrush.constants.TimeConstants;
import com.glotrush.dto.response.MistakeQuestionResponse;
import com.glotrush.dto.response.MistakeQuestionResponse.MistakeQuestionResponseBuilder;
import com.glotrush.dto.response.answers.UserMistakeListAnswerResponse;
import com.glotrush.dto.response.answers.UserMistakeRetryListResponse;
import com.glotrush.dto.response.answers.UserMistakeRetryListResponse.UserMistakeRetryListResponseBuilder;
import com.glotrush.entities.UserMistake;
import com.glotrush.entities.exercice.FlashcardEntity;
import com.glotrush.entities.exercice.MatchingPairEntity;
import com.glotrush.entities.exercice.QcmQuestionEntity;
import com.glotrush.entities.exercice.SortingExerciseEntity;
import com.glotrush.exceptions.QuestionNotFoundException;
import com.glotrush.mapping.model.LessonTypeMaps;
import com.glotrush.utils.LocaleUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserMistakeMapper {

    private final UserMistakeBuilder userMistakeBuilder;
    private final MessageSource messageSource;

    public List<MistakeQuestionResponse> toMistakeQuestionResponses(List<UserMistake> mistakes, LessonTypeMaps maps) {
        return mistakes.stream().map(mistake -> toMistakeQuestionResponse(mistake, maps)).toList();
    }

    public List<UserMistakeListAnswerResponse> toListResponses(List<UserMistake> mistakes, LessonTypeMaps maps) {
        return mistakes.stream().map(mistake -> toListItemResponse(mistake, maps)).toList();
    }

    public List<UserMistakeRetryListResponse> toRetryListResponses(List<UserMistake> mistakes, LessonTypeMaps maps) {
        return mistakes.stream().map(mistake -> toRetryListResponse(mistake, maps)).toList();
    }

    public UserMistakeRetryListResponse toRetryListResponse(UserMistake mistake, LessonTypeMaps lessonType) {
        UUID questionId = mistake.getQuestionId();
        UserMistakeRetryListResponseBuilder builder = userMistakeBuilder.retryListResponse(mistake, formatNextReview(mistake));

        return switch (mistake.getLessonType()) {
            case FLASHCARD -> {
                FlashcardEntity flashcard = lessonType.flashcards().get(questionId);
                if (flashcard == null) {
                    throw new QuestionNotFoundException(messageSource.getMessage("error.question.not_found", null, LocaleUtils.getCurrentLocale()));
                }
                yield builder.answeredQuestion(flashcard.getFront()).build();
            }

            case QCM -> {
                QcmQuestionEntity qcm = lessonType.qcms().get(questionId);
                if (qcm == null){
                    throw new QuestionNotFoundException(messageSource.getMessage("error.question.not_found", null, LocaleUtils.getCurrentLocale()));
                }
                yield builder
                    .answeredQuestion(qcm.getQuestion())
                    .options(qcm.getOptions())
                    .build();
            }

            case MATCHING_PAIR -> {
                MatchingPairEntity matchingPair = lessonType.matchingPairs().get(questionId);
                if (matchingPair == null){
                    throw new QuestionNotFoundException(messageSource.getMessage("error.question.not_found", null, LocaleUtils.getCurrentLocale()));
                }
                yield builder.answeredQuestion(matchingPair.getItem1()).build();
            }

            case SORTING_EXERCISE -> {
                SortingExerciseEntity sortingExercise = lessonType.sortingExercises().get(questionId);
                if (sortingExercise == null){
                    throw new QuestionNotFoundException(messageSource.getMessage("error.question.not_found", null, LocaleUtils.getCurrentLocale()));
                }
                yield builder
                    .answeredQuestion(messageSource.getMessage("label.sorting_exercise", null, LocaleUtils.getCurrentLocale()) + " : " + String.join(", ", sortingExercise.getItems()))
                    .build();
            }
        };
    }

    public String formatNextReview(UserMistake answerMistake) {
        LocalDateTime nextReview = answerMistake.getNextReviewAt();
        if (nextReview == null || !nextReview.isAfter(LocalDateTime.now())) {
            return messageSource.getMessage("label.review_now", null, LocaleUtils.getCurrentLocale());
        } 

        long minutes = Duration.between(LocalDateTime.now(), nextReview).toMinutes();
        if (minutes < TimeConstants.SIXTY_MINUTES) {
            return messageSource.getMessage("label.review_in_minutes", new Object[]{minutes}, LocaleUtils.getCurrentLocale());
        }
        long hours = minutes / TimeConstants.SIXTY_MINUTES;
        if (hours < TimeConstants.TWENTY_FOUR_HOURS){
            return messageSource.getMessage("label.review_in_hours", new Object[]{hours}, LocaleUtils.getCurrentLocale());
        }
        return messageSource.getMessage("label.review_in_days", new Object[]{hours / TimeConstants.TWENTY_FOUR_HOURS}, LocaleUtils.getCurrentLocale());
    }


    private MistakeQuestionResponse toMistakeQuestionResponse(UserMistake answerMistake, LessonTypeMaps lessonType) {
        UUID questionId = answerMistake.getQuestionId();
        MistakeQuestionResponseBuilder builder = MistakeQuestionResponse.builder()
            .userMistakeId(answerMistake.getId())
            .questionId(questionId)
            .lessonType(answerMistake.getLessonType());

        return switch (answerMistake.getLessonType()) {
            case FLASHCARD -> {
                FlashcardEntity flashCard = lessonType.flashcards().get(questionId);
                if (flashCard == null){
                    throw new QuestionNotFoundException(messageSource.getMessage("error.question.not_found", null, LocaleUtils.getCurrentLocale()));
                }
                yield builder.front(flashCard.getFront()).frontLanguage(flashCard.getFrontLanguage()).backLanguage(flashCard.getBackLanguage()).build();
            }
            case QCM -> {
                QcmQuestionEntity qcm = lessonType.qcms().get(questionId);
                if (qcm == null){
                    throw new QuestionNotFoundException(messageSource.getMessage("error.question.not_found", null, LocaleUtils.getCurrentLocale()));
                }
                yield builder.question(qcm.getQuestion()).options(qcm.getOptions()).build();
            }
            case MATCHING_PAIR -> {
                MatchingPairEntity matchingPair = lessonType.matchingPairs().get(questionId);
                if (matchingPair == null){
                    throw new QuestionNotFoundException(messageSource.getMessage("error.question.not_found", null, LocaleUtils.getCurrentLocale()));
                }
                yield builder.item1(matchingPair.getItem1()).build();
            }
            case SORTING_EXERCISE -> {
                SortingExerciseEntity sortingExercise = lessonType.sortingExercises().get(questionId);
                if (sortingExercise == null){
                    throw new QuestionNotFoundException(messageSource.getMessage("error.question.not_found", null, LocaleUtils.getCurrentLocale()));
                }
                yield builder.items(sortingExercise.getItems()).build();
            }
        };
    }

    private UserMistakeListAnswerResponse toListItemResponse(UserMistake mistakeAnswer, LessonTypeMaps lessonType) {
        UUID questionId = mistakeAnswer.getQuestionId();
        UserMistakeListAnswerResponse.UserMistakeListAnswerResponseBuilder builder = userMistakeBuilder.listAnswerResponse(mistakeAnswer, formatNextReview(mistakeAnswer));

        return switch (mistakeAnswer.getLessonType()) {
            case FLASHCARD -> applyFlashcard(builder, lessonType.flashcards().get(questionId), questionId);
            case QCM -> applyQcm(builder, lessonType.qcms().get(questionId), questionId);
            case MATCHING_PAIR -> applyMatchingPair(builder, lessonType.matchingPairs().get(questionId), questionId);
            case SORTING_EXERCISE -> applySortingExercise(builder, lessonType.sortingExercises().get(questionId), questionId);
        };
    }

    private UserMistakeListAnswerResponse applyFlashcard(UserMistakeListAnswerResponse.UserMistakeListAnswerResponseBuilder builder, FlashcardEntity flashCard, UUID questionId) {
        if (flashCard == null){
            throw new QuestionNotFoundException(messageSource.getMessage("error.question.not_found", null, LocaleUtils.getCurrentLocale()));
        }
        return builder.questionLabel(flashCard.getFront()).correctAnswer(flashCard.getBack()).build();
    }

    private UserMistakeListAnswerResponse applyQcm(UserMistakeListAnswerResponse.UserMistakeListAnswerResponseBuilder builder, QcmQuestionEntity qcm, UUID questionId) {
        if (qcm == null){
            throw new QuestionNotFoundException(messageSource.getMessage("error.question.not_found", null, LocaleUtils.getCurrentLocale()));
        }
        return builder.questionLabel(qcm.getQuestion())
                .options(qcm.getOptions())
                .correctAnswer(qcm.getOptions().get(qcm.getCorrectOptionIndex()))
                .build();
    }

    private UserMistakeListAnswerResponse applyMatchingPair(UserMistakeListAnswerResponse.UserMistakeListAnswerResponseBuilder builder, MatchingPairEntity matchingPair, UUID questionId) {
        if (matchingPair == null){
            throw new QuestionNotFoundException(messageSource.getMessage("error.question.not_found", null, LocaleUtils.getCurrentLocale()));
        }
        return builder.questionLabel(matchingPair.getItem1()).correctAnswer(matchingPair.getItem2()).build();
    }

    private UserMistakeListAnswerResponse applySortingExercise(UserMistakeListAnswerResponse.UserMistakeListAnswerResponseBuilder builder, SortingExerciseEntity sortingExercise, UUID questionId) {
        if (sortingExercise == null){
            throw new QuestionNotFoundException(messageSource.getMessage("error.question.not_found", null, LocaleUtils.getCurrentLocale()));
        }
        String ordered = sortingExercise.getCorrectOrder().stream().map(i -> sortingExercise.getItems().get(i)).collect(Collectors.joining(" → "));
        return builder.questionLabel(messageSource.getMessage("label.sorting_exercise", null, LocaleUtils.getCurrentLocale()) + " : " + String.join(", ", sortingExercise.getItems()))
                 .correctAnswer(ordered)
                 .build();
    }
}
