package com.glotrush.dto.request.challenge;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChallengeResponseRequest {

    @NotNull
    private Double score;

    @NotNull
    private Long timePassed;
    
}
