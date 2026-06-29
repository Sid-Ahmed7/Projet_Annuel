package com.glotrush.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.glotrush.entities.challenge.Challenge;
import com.glotrush.enumerations.ChallengeStatus;
import com.glotrush.enumerations.ChallengeType;

public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {
   
    @Query("SELECT DISTINCT c FROM Challenge c LEFT JOIN c.participants p WHERE c.challenger.id = :accountId OR c.challenged.id = :accountId OR p.account.id = :accountId ORDER BY c.createdAt DESC")
    List<Challenge> findAllByAccountId(@Param("accountId") UUID accountId);

    List<Challenge> findByChallengeTypeAndChallengeStatus(ChallengeType challengeType, ChallengeStatus challengeStatus);

    @Query("SELECT c FROM Challenge c WHERE c.challengeType = 'PUBLIC' AND c.challengeStatus = 'ACTIVE' AND (:activeLanguageId IS NULL OR c.language.id = :activeLanguageId) AND (:nativeLanguageId IS NULL OR c.sourceLanguage.id = :nativeLanguageId)")
    List<Challenge> findPublicChallengesWithFilters(@Param("activeLanguageId") UUID activeLanguageId,@Param("nativeLanguageId") UUID nativeLanguageId);

    @Query("SELECT c FROM Challenge c WHERE c.challengeStatus IN ('PENDING', 'ACTIVE') AND c.expiresAt <= :now")
    List<Challenge> findExpiredChallenges(@Param("now") LocalDateTime now);
} 