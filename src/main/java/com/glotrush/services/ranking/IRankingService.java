package com.glotrush.services.ranking;

import java.util.UUID;

import com.glotrush.dto.response.RankingResponse;

public interface IRankingService {

    RankingResponse getGlobalRanking(UUID accountId, Integer page, Integer size);
    RankingResponse getLanguageRanking(UUID accountId, UUID languageId, Integer page, Integer size);
    RankingResponse getFriendsRanking(UUID accountId, Integer page, Integer size);
}
