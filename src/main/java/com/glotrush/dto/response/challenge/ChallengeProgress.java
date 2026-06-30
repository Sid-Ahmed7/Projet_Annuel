package com.glotrush.dto.response.challenge;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeProgress {
    private UUID challengeId;
    private UUID accountId;
    private String username;
    private UUID id;
    private String front;
    private String back;
    private String frontLanguage;
    private String backLanguage;
    private Integer timeLimitSeconds;
    private String photoUrl;
    private Double score;
    private Integer questionAnswered;
    private Integer totalQuestions;
    private Long timePassed;
    private Integer finalRank;
    private Integer xpGained;
}
