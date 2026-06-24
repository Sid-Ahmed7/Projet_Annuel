package com.glotrush.mapping.model;

import java.util.Map;
import java.util.UUID;

import com.glotrush.entities.exercice.FlashcardEntity;
import com.glotrush.entities.exercice.MatchingPairEntity;
import com.glotrush.entities.exercice.QcmQuestionEntity;
import com.glotrush.entities.exercice.SortingExerciseEntity;

public record LessonTypeMaps(
    Map<UUID, FlashcardEntity> flashcards,
    Map<UUID, QcmQuestionEntity> qcms,
    Map<UUID, MatchingPairEntity> matchingPairs,
    Map<UUID, SortingExerciseEntity> sortingExercises
) {}
