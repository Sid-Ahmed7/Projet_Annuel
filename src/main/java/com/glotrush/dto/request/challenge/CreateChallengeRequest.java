package com.glotrush.dto.request.challenge;

import java.util.UUID;

import com.glotrush.enumerations.ChallengeType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateChallengeRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotNull
    private ChallengeType challengeType;

    @NotNull
    private UUID lessonId;

    @Min(1)
    private Integer questionCount;

    private UUID challengedId;
    
}
