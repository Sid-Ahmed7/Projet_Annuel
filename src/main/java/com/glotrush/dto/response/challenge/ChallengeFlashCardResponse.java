package com.glotrush.dto.response.challenge;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeFlashCardResponse {
    private UUID id;
    private String front;
    private String back;
    private String frontLanguage;
    private String backLanguage;
    private Integer timeLimitSeconds;
}
