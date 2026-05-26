package com.glotrush.dto.response.challenge;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeParticipantResponse {

    private UUID accountId;
    private String username;
    private String photoUrl;
    private Double score;
    private Long timePassed;
    private Integer xpGained;
    private Integer finalRank;
    private LocalDateTime completedAt;
    private boolean hasCompleted;
}
