package com.glotrush.dto.response.exercice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FlashcardExamResponse {
    private UUID id;
    @NotNull
    @NotEmpty
    private String front;
    @NotNull
    @NotEmpty
    private String frontLanguage;
    @NotNull
    @NotEmpty
    private String backLanguage;
    // On cache le "back" pour l'examen
}