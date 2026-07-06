package com.glotrush.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.glotrush.entities.challenge.ChallengeParticipant;
import com.glotrush.enumerations.ChallengeType;

public interface ChallengeParticipantsRepository extends JpaRepository<ChallengeParticipant, UUID> {

    Optional<ChallengeParticipant> findByChallengeIdAndAccountId(UUID challengeId, UUID accountId);

    List<ChallengeParticipant> findByChallengeIdOrderByScoreDescTimePassedAsc(UUID challengeId);

    @Query("SELECT COALESCE(SUM(cp.xpGained), 0) FROM ChallengeParticipant cp WHERE cp.account.id = :accountId AND cp.challenge.challengeType = :challengeType AND cp.scoreRecorded = true")
    Long sumXpGained(@Param("accountId") UUID accountId, @Param("challengeType") ChallengeType challengeType);

    @Modifying
    @Query("DELETE FROM ChallengeParticipant cp WHERE cp.account.id = :accountId")
    void deleteByAccount_Id(@Param("accountId") UUID accountId);
}
