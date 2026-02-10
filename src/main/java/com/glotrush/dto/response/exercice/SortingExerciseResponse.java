package com.glotrush.dto.response.exercice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SortingExerciseResponse {
    private UUID id;
    @NotNull
    @NotEmpty
    private List<String> items; // Mots à trier
    @NotNull
    @NotEmpty
    private List<Integer> correctOrder; // Ordre correct
}