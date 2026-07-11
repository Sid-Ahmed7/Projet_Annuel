package com.glotrush.dto.request.challenge;

import java.util.List;
import java.util.UUID;

import com.glotrush.enumerations.ChallengeType;
import com.glotrush.enumerations.LessonType;

import jakarta.validation.constraints.Min;
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

    private UUID lessonId;
    private UUID challengedId;

    private UUID languageId;
    private UUID sourceLanguageId;

    private LessonType lessonType;

    @Min(1)
    private Integer questionCount;

    private List<ChallengeQcmRequest> qcm;
    private List<ChallengeFlashCardRequest> flashcards;
    private List<ChallengeMatchingPairRequest> matchingPairs;
    private List<ChallengeSortingExerciseRequest> sortingExercises;
}
