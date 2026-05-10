package com.glotrush.dto.request.challenge;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChallengeSortingExerciseRequest {

    @NotNull
    private List<String> items;

    @NotNull
    private List<Integer> correctOrder;

}
