package com.glotrush.dto.response.challenge;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeSortingExerciseResponse {
    private UUID id;
    private List<String> items;
    private List<Integer> correctOrder;
}
