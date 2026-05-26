package com.glotrush.websocket;

import java.util.UUID;

public interface IRankingWsService {
    void sendRankingGlobal();
    void sendRankingLanguage(UUID languageId);
}
