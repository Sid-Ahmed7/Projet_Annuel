package com.glotrush.dto.request.exercice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SortingExerciseRequest {
    @NotNull
    @NotEmpty
    private List<String> items; // Mots à trier
    @NotNull
    @NotEmpty
    private List<Integer> correctOrder; // Ordre correct
}