package com.glotrush.dto.request.challenge;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChallengeQcmRequest {

    @NotBlank
    private String question;

    @NotNull
    private List<String> options;

    @NotNull
    private Integer correctOptionIndex;

    private String explanation;
}
