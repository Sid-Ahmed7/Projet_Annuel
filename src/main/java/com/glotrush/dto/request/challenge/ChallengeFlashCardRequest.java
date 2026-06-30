package com.glotrush.dto.request.challenge;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChallengeFlashCardRequest {

    @NotBlank
    private String front;

    @NotBlank
    private String back;

    private String frontLanguage;
    private String backLanguage;
    private Integer timeLimitSeconds = 30;
}
