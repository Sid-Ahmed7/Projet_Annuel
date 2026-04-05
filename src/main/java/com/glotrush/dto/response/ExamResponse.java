package com.glotrush.dto.response;

import com.glotrush.dto.response.exercice.FlashcardExamResponse;
import com.glotrush.dto.response.exercice.MatchingPairResponse;
import com.glotrush.dto.response.exercice.QcmQuestionExamResponse;
import com.glotrush.dto.response.exercice.SortingExerciseExamResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponse {
    private UUID topicId;
    private String topicName;
    private List<QcmQuestionExamResponse> qcmQuestions;
    private List<FlashcardExamResponse> flashcards;
    private List<MatchingPairResponse> matchingPairs;
    private List<SortingExerciseExamResponse> sortingExercises;
}
