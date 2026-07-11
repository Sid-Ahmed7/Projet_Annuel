package com.glotrush.dto.response.ai;

import java.util.List;

import com.glotrush.dto.request.challenge.ChallengeFlashCardRequest;
import com.glotrush.dto.request.challenge.ChallengeMatchingPairRequest;
import com.glotrush.dto.request.challenge.ChallengeQcmRequest;
import com.glotrush.dto.request.challenge.ChallengeSortingExerciseRequest;
import com.glotrush.enumerations.LessonType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChallengeContentResponse {

    private LessonType lessonType;
    
    private List<ChallengeQcmRequest> qcm;
    
    private List<ChallengeFlashCardRequest> flashcards;
    
    private List<ChallengeMatchingPairRequest> matchingPairs;
    
    private List<ChallengeSortingExerciseRequest> sortingExercises;
}
