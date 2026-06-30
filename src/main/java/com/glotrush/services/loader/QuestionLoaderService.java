package com.glotrush.services.loader;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.glotrush.entities.UserMistake;
import com.glotrush.entities.exercice.FlashcardEntity;
import com.glotrush.entities.exercice.MatchingPairEntity;
import com.glotrush.entities.exercice.QcmQuestionEntity;
import com.glotrush.entities.exercice.SortingExerciseEntity;
import com.glotrush.entities.exercice.InteractiveQuestionEntity;
import com.glotrush.enumerations.LessonType;
import com.glotrush.mapping.model.LessonTypeMaps;
import com.glotrush.repositories.exercice.FlashcardRepository;
import com.glotrush.repositories.exercice.MatchingPairRepository;
import com.glotrush.repositories.exercice.QcmQuestionRepository;
import com.glotrush.repositories.exercice.SortingExerciseRepository;
import com.glotrush.repositories.exercice.InteractiveQuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionLoaderService implements IQuestionLoaderService {

    private final FlashcardRepository flashcardRepository;
    private final QcmQuestionRepository qcmQuestionRepository;
    private final MatchingPairRepository matchingPairRepository;
    private final SortingExerciseRepository sortingExerciseRepository;
    private final InteractiveQuestionRepository interactiveQuestionRepository;

    @Override
    public LessonTypeMaps loadQuestionMaps(List<UserMistake> mistakes) {
        Map<LessonType, Set<UUID>> idsByType = mistakes.stream().collect(Collectors.groupingBy(
                UserMistake::getLessonType,
                Collectors.mapping(UserMistake::getQuestionId, Collectors.toSet())
            ));

        return new LessonTypeMaps(
            fetchById(idsByType.get(LessonType.FLASHCARD), flashcardRepository, FlashcardEntity::getId),
            fetchById(idsByType.get(LessonType.QCM), qcmQuestionRepository, QcmQuestionEntity::getId),
            fetchById(idsByType.get(LessonType.MATCHING_PAIR), matchingPairRepository, MatchingPairEntity::getId),
            fetchById(idsByType.get(LessonType.SORTING_EXERCISE), sortingExerciseRepository, SortingExerciseEntity::getId),
            fetchById(idsByType.get(LessonType.INTERACTIVE), interactiveQuestionRepository, InteractiveQuestionEntity::getId)
        );
    }

    @Override
    public <T> Map<UUID, T> fetchById(Collection<UUID> ids, JpaRepository<T, UUID> repo, Function<T, UUID> getId) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return repo.findAllById(ids).stream()
            .collect(Collectors.toMap(getId, Function.identity()));
    }
}
