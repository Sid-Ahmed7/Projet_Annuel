package com.glotrush.dto.response.lesson;

import com.glotrush.dto.response.LessonResponse;
import com.glotrush.dto.response.exercice.SortingExerciseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class SortingExerciseLessonResponse extends LessonResponse {
    private List<SortingExerciseResponse> sortingExercise;
}
