package com.glotrush.websocket.challenge;

import java.util.Map;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.glotrush.dto.request.challenge.ChallengeNotificationRequest;
import com.glotrush.dto.response.challenge.ChallengeProgress;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChallengeWsService implements IChallengeWsService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendChallengeProgress(UUID challengeId, ChallengeProgress progress) {
        messagingTemplate.convertAndSend("/topic/challenge/" + challengeId + "/progress", progress); 
    }

    @Override
    public void sendChallengeResult(UUID challengeId, ChallengeProgress result) {
        messagingTemplate.convertAndSend("/topic/challenge/" + challengeId + "/result", result);
    }

    @Override
    public void sendNotificationToChallenged(UUID accountId, ChallengeNotificationRequest request) {
        messagingTemplate.convertAndSend("/topic/duel." + accountId, request);
    }

    @Override
    public void sendNotificationToStartChallenge(UUID challengeId) {
        messagingTemplate.convertAndSend("/topic/challenge/" + challengeId + "/start", Map.of("status", "STARTED"));
    }
}
