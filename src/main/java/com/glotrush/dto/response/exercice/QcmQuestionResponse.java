package com.glotrush.dto.response.exercice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class QcmQuestionResponse {
    private UUID id;
    @NotNull
    @NotEmpty
    private String question;
    @NotNull
    @NotEmpty
    private List<String> options; // Réponses possibles
    @NotNull
    private Integer correctOptionIndex; // Index de la bonne réponse
    @NotNull
    @NotEmpty
    private String explanation; // Explication de la réponse
}