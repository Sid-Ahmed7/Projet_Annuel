package com.glotrush.dto.response.challenge;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeQcmResponse {
    private UUID id;
    private String question;
    private List<String> options;
    private Integer correctOptionIndex;
    private String explanation;
}
