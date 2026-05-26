package com.glotrush.services.ranking;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.glotrush.builder.RankingBuilder;
import com.glotrush.dto.request.AccountXpRequest;
import com.glotrush.dto.response.RankedUserResponse;
import com.glotrush.dto.response.RankingResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.UserProfile;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.repositories.UserProgressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingService implements IRankingService {

    private final UserProgressRepository userProgressRepository;
    private final AccountsRepository accountsRepository;
    private final UserProfileRepository userProfileRepository;
    private final RankingBuilder rankingBuilder;
    
    
    @Override
    public RankingResponse getGlobalRanking(UUID accountId, Integer page, Integer size) {
        List<AccountXpRequest> ranking = userProgressRepository.findGlobalRanking(PageRequest.of(page, size));
        Long totalParticipants = userProgressRepository.countGlobalParticipants();
        List<UUID> accountIds = ranking.stream().map(AccountXpRequest::getAccountId).toList();

        Map<UUID, Accounts> accountsMap = toAccountsMap(accountIds);
        Map<UUID, UserProfile> userProfileMap = toProfilesMap(accountIds);

        List<RankedUserResponse> rankedUsers = rankingBuilder.buildRankedUser(ranking, page * size, accountId, accountsMap, userProfileMap);
        
        Long currentUserXp = userProgressRepository.getXpByAccountId(accountId);
        Long currentUserRank = userProgressRepository.findGlobalRankByAccountId(accountId);
        Accounts currentUserAccount = accountsRepository.findById(accountId).orElse(null);
        UserProfile currentUserProfile = userProfileRepository.findByAccount_Id(accountId).orElse(null);
    
        RankedUserResponse currentUserRanked = rankingBuilder.buildSingleRankedUser(accountId, currentUserRank.intValue(), currentUserXp, currentUserAccount, currentUserProfile);
        Long xpToNextRank = null;

        if(currentUserRank > 1) {
            List<AccountXpRequest> nextRank = userProgressRepository.findGlobalRanking(PageRequest.of(currentUserRank.intValue() - 2, 1));
            if(!nextRank.isEmpty()) {
             long aboveXp = nextRank.get(0).getTotalXP() != null ? nextRank.get(0).getTotalXP() : 0L;
             xpToNextRank = aboveXp - (currentUserXp != null ? currentUserXp : 0L);
            }
        }
        return RankingResponse.builder()
                .rankedUser(rankedUsers).currentUserRank(currentUserRanked)
                .totalParticipants(totalParticipants)
                .currentPage(page)
                .pageSize(size)
                .xpToNextRank(xpToNextRank)
                .build();

    }

    @Override
    public RankingResponse getLanguageRanking(UUID accountId, UUID languageId, Integer page, Integer size) {
    
        List<AccountXpRequest> ranking = userProgressRepository.findLanguageRanking(languageId, PageRequest.of(page, size));
        Long totalParticipants = userProgressRepository.countLanguageParticipants(languageId);
        List<UUID> accountIds = ranking.stream().map(AccountXpRequest::getAccountId).toList();
        Map<UUID, Accounts> accountsMap = toAccountsMap(accountIds);
        Map<UUID, UserProfile> userProfileMap = toProfilesMap(accountIds);
        List<RankedUserResponse> rankedUsers = rankingBuilder.buildRankedUser(ranking, page * size, accountId, accountsMap, userProfileMap);
        Long currentUserXp = userProgressRepository.getLanguageXpByAccountId(accountId, languageId);
        Long currentUserRank = userProgressRepository.findLanguageRankByAccountId(accountId, languageId);
        Accounts currentUserAccount = accountsRepository.findById(accountId).orElse(null);
        UserProfile currentUserProfile = userProfileRepository.findByAccount_Id(accountId).orElse(null);
    
        RankedUserResponse currentUserRanked = rankingBuilder.buildSingleRankedUser(accountId, currentUserRank.intValue(), currentUserXp, currentUserAccount, currentUserProfile);
        
        Long xpToNextRank = null;

        if(currentUserRank > 1) {
            List<AccountXpRequest> nextRank = userProgressRepository.findLanguageRanking(languageId, PageRequest.of(currentUserRank.intValue() - 2, 1));
            if(!nextRank.isEmpty()) {
             long aboveXp = nextRank.get(0).getTotalXP() != null ? nextRank.get(0).getTotalXP() : 0L;
             xpToNextRank = aboveXp - (currentUserXp != null ? currentUserXp : 0L);
            }
        }
        
        return RankingResponse.builder()
                .rankedUser(rankedUsers).currentUserRank(currentUserRanked)
                .totalParticipants(totalParticipants)
                .currentPage(page)
                .pageSize(size)
                .xpToNextRank(xpToNextRank)
                .build();
    }

    private Map<UUID, Accounts> toAccountsMap(List<UUID> ids) {
        return accountsRepository.findAllById(ids).stream().collect(Collectors.toMap(Accounts::getId, a -> a));
    }

    private Map<UUID, UserProfile> toProfilesMap(List<UUID> ids) {
        return userProfileRepository.findByAccount_IdIn(ids).stream().collect(Collectors.toMap(p -> p.getAccount().getId(), p -> p));
    }
    
    
}
