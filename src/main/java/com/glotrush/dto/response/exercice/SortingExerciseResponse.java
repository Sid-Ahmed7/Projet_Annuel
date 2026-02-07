package com.glotrush.dto.response.exercice;

import com.glotrush.dto.request.LessonRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SortingExerciseResponse extends LessonRequest {
    @NotNull
    @NotEmpty
    private List<String> items; // Mots à trier
    @NotNull
    @NotEmpty
    private List<Integer> correctOrder; // Ordre correct
}