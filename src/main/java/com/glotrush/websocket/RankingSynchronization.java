package com.glotrush.websocket;

import java.util.UUID;

import org.springframework.transaction.support.TransactionSynchronization;

public class RankingSynchronization implements TransactionSynchronization {

    private final IRankingWsService rankingWsService;
    private final UUID languageId;

    public RankingSynchronization(IRankingWsService rankingWsService, UUID languageId) {
        this.rankingWsService = rankingWsService;
        this.languageId = languageId;
    }

    @Override
    public void afterCommit() {
        rankingWsService.sendRankingGlobal();
        rankingWsService.sendRankingLanguage(languageId);
    }
}
