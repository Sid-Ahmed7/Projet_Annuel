package com.glotrush.repositories.ai;

import com.glotrush.entities.ai.AIGenerationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AIGenerationLogRepository extends JpaRepository<AIGenerationLog, UUID> {

    @Query("SELECT COUNT(l) FROM AIGenerationLog l WHERE l.accountId = :accountId AND l.createdAt >= :startDate")
    long countByAccountIdAndCreatedAtAfter(@Param("accountId") UUID accountId, @Param("startDate") LocalDateTime startDate);
    void deleteByAccountId(UUID accountId);
}
