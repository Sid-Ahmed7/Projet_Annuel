package com.glotrush.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.glotrush.dto.request.AccountXpRequest;
import com.glotrush.dto.response.RankedUserResponse;
import com.glotrush.dto.response.RankingResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.UserProfile;
import com.glotrush.utils.LevelUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RankingBuilder {


    public List<RankedUserResponse> buildRankedUser(List<AccountXpRequest> ranking, int offset, UUID accountID, Map<UUID, Accounts> accountsMap, Map<UUID, UserProfile> userProfileMap) {
        List<RankedUserResponse> rankedUsers = new ArrayList<>();
        int currentRank = offset + 1;
        Long previousXp = null;

        for (int i = 0; i < ranking.size(); i++) {
            AccountXpRequest accountXp = ranking.get(i);
            Accounts account = accountsMap.get(accountXp.getAccountId());
            UserProfile profile = userProfileMap.get(accountXp.getAccountId());
            long xp = accountXp.getTotalXP() != null ? accountXp.getTotalXP() : 0L;

            if (previousXp == null || xp != previousXp.longValue()) {
                currentRank = offset + i + 1;
            }
            previousXp = xp;

            rankedUsers.add(RankedUserResponse.builder()
                .rank(currentRank)
                .accountId(accountXp.getAccountId())
                .username(account.getUsername())
                .photoUrl(profile != null ? profile.getPhotoUrl() : null)
                .totalXP(xp)
                .level(LevelUtils.calculateLevel(xp))
                .levelProgressPercentage(LevelUtils.calculateLevelProgressPercentage(xp))
                .isCurrentUser(accountID.equals(accountXp.getAccountId()))
                .build());
        }
        return rankedUsers;
    }


    public RankingResponse buildRankingResponse(List<RankedUserResponse> rankedUsers,RankedUserResponse currentUserRanked,Long totalParticipants,Integer page,Integer size,Long currentUserRank,Long currentUserXp,Supplier<List<AccountXpRequest>> nextRankFetcher) {

        Long xpToNextRank = null;
        if (currentUserRank > 1) {
            List<AccountXpRequest> nextRank = nextRankFetcher.get();
            if (!nextRank.isEmpty()) {
                long aboveXp = nextRank.get(0).getTotalXP() != null ? nextRank.get(0).getTotalXP() : 0L;
                xpToNextRank = aboveXp - (currentUserXp != null ? currentUserXp : 0L);
            }
        }

        return RankingResponse.builder()
                .rankedUser(rankedUsers)
                .currentUserRank(currentUserRanked)
                .totalParticipants(totalParticipants)
                .currentPage(page)
                .pageSize(size)
                .xpToNextRank(xpToNextRank)
                .build();
    }

    public RankedUserResponse buildSingleRankedUser(UUID accountID, int rank, Long xp, Accounts accounts, UserProfile userProfile) {
        if(accounts == null) {
            return null;
        }
        long xpValue = xp != null ? xp : 0L;
        return RankedUserResponse.builder()
                .rank(rank)
                .accountId(accounts.getId())
                .username(accounts.getUsername())
                .photoUrl(userProfile != null ? userProfile.getPhotoUrl() : null)
                .totalXP(xpValue)
                .level(LevelUtils.calculateLevel(xpValue))
                .levelProgressPercentage(LevelUtils.calculateLevelProgressPercentage(xpValue))
                .isCurrentUser(accountID.equals(accounts.getId()))
                .build();
    }



    
}
