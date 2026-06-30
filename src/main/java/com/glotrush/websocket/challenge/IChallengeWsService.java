package com.glotrush.websocket.challenge;

import java.util.UUID;

import com.glotrush.dto.request.challenge.ChallengeNotificationRequest;
import com.glotrush.dto.response.challenge.ChallengeProgress;

public interface IChallengeWsService {

    void sendChallengeProgress(UUID challengeId, ChallengeProgress progress);
    void sendChallengeResult(UUID challengeId, ChallengeProgress result); 
    void sendNotificationToChallenged(UUID accountId, ChallengeNotificationRequest request);
    void sendNotificationToStartChallenge(UUID challengeId);
}
