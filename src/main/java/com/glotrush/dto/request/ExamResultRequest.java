package com.glotrush.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultRequest {
    private List<FlashcardAnswerRequest> flashcardAnswers;
    private List<QcmAnswerRequest> qcmAnswers;
    private List<MatchingPairAnswerRequest> matchingPairAnswers;
    private List<SortingExerciseAnswerRequest> sortingExerciseAnswers;
}
