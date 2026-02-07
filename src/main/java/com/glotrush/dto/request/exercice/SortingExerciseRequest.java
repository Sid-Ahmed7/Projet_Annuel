package com.glotrush.dto.request.exercice;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.entities.lesson.SortingExerciseLesson;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class SortingExerciseRequest extends LessonRequest {
    @NotNull
    @NotEmpty
    private List<String> items; // Mots à trier
    @NotNull
    @NotEmpty
    private List<Integer> correctOrder; // Ordre correct
}