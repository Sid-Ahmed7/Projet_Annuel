package com.glotrush.dto.request.challenge;

import java.util.List;
import java.util.UUID;

import com.glotrush.enumerations.ChallengeType;
import com.glotrush.enumerations.LessonType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateChallengeRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotNull
    private ChallengeType challengeType;

    @NotNull
    private UUID languageId;

    @NotNull
    private LessonType lessonType;

    private UUID challengedId;

    private List<ChallengeQcmRequest> qcm;

    private List<ChallengeFlashCardRequest> flashcards;

    private List<ChallengeMatchingPairRequest> matchingPairs;

    private List<ChallengeSortingExerciseRequest> sortingExercises;
    
}
