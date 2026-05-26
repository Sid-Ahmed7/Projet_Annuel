package com.glotrush.dto.response.challenge;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeUserResponse {

    private UUID id;
    private String username;
    private String photoUrl;
}
