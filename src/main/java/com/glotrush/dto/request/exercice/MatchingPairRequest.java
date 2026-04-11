package com.glotrush.dto.request.exercice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class MatchingPairRequest {
    @NotNull
    @NotEmpty
    private String item1;
    @NotNull
    @NotEmpty
    private String item2;
}
