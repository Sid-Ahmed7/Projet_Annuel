package com.glotrush.dto.request.challenge;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChallengeMatchingPairRequest {

    @NotBlank
    private String item1;

    @NotBlank
    private String item2;
}
