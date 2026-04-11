package com.glotrush.dto.response.exercice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class QcmQuestionExamResponse {
    private UUID id;
    @NotNull
    @NotEmpty
    private String question;
    @NotNull
    @NotEmpty
    private List<String> options; // Réponses possibles sans l'index de la bonne réponse ni l'explication
}