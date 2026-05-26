package com.glotrush.dto.response.challenge;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeMatchingPairResponse {
    private UUID id;
    private String item1;
    private String item2;
}
