package com.glotrush.dto.request.challenge;

import java.util.List;

import com.glotrush.dto.request.FlashcardAnswerRequest;
import com.glotrush.dto.request.MatchingPairAnswerRequest;
import com.glotrush.dto.request.QcmAnswerRequest;
import com.glotrush.dto.request.SortingExerciseAnswerRequest;

import lombok.Data;

@Data
public class ChallengeResponseRequest {

    private Double score;

    private Long timePassed;

    private List<FlashcardAnswerRequest> flashcardAnswers;

    private List<QcmAnswerRequest> qcmAnswers;

    private List<MatchingPairAnswerRequest> matchingPairAnswers;

    private List<SortingExerciseAnswerRequest> sortingExerciseAnswers;

}
