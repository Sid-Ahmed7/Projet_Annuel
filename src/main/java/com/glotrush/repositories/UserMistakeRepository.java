package com.glotrush.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.glotrush.entities.UserMistake;

public interface UserMistakeRepository extends JpaRepository<UserMistake, UUID> {
    
    @Query("SELECT m FROM UserMistake m WHERE m.account.id = :accountId AND m.isResolved = false AND m.nextReviewAt <= :now ORDER BY m.createdAt ASC")
    List<UserMistake> findPendingReviewMistakes(@Param("accountId") UUID accountId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(m) FROM UserMistake m WHERE m.account.id = :accountId AND m.isResolved = false AND m.nextReviewAt <= :now")
    long countPendingReviewMistakes(@Param("accountId") UUID accountId, @Param("now") LocalDateTime now);
    Optional<UserMistake> findByAccount_IdAndQuestionIdAndIsResolvedFalse(UUID accountId, UUID questionId);
    List<UserMistake> findByAccount_IdAndIsResolvedFalseOrderByCreatedAtAsc(UUID accountId);
    long countByAccount_IdAndIsResolvedFalse(UUID accountId);

    void deleteByAccount_Id(UUID accountId);
}

