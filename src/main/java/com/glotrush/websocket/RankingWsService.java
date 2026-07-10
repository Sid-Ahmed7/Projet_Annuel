package com.glotrush.websocket;

import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingWsService implements IRankingWsService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendRankingGlobal() {
        messagingTemplate.convertAndSend("/topic/ranking.global", "");
    }

    @Override
    public void sendRankingLanguage(UUID languageId) {
        messagingTemplate.convertAndSend("/topic/ranking.language." + languageId, "");
    }
    
}
