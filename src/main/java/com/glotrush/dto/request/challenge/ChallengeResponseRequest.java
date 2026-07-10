package com.glotrush.dto.request.challenge;

import java.util.List;

import com.glotrush.dto.request.FlashcardAnswerRequest;
import com.glotrush.dto.request.QcmAnswerRequest;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChallengeResponseRequest {

    private Double score;

    @NotNull
    private Long timePassed;

    private List<FlashcardAnswerRequest> flashcardAnswers;

    private List<QcmAnswerRequest> qcmAnswers;

}
