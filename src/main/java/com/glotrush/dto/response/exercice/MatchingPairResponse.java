package com.glotrush.dto.response.exercice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MatchingPairResponse {
    private UUID id;
    @NotNull
    @NotEmpty
    private String item1;
    @NotNull
    @NotEmpty
    private String item2;
}
