package com.glotrush.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.Accounts;
import com.glotrush.entities.UserProgress;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {
    
    List<UserProgress> findByAccount_Id(UUID accountId);

    Optional<UserProgress> findByAccount_IdAndTopic_Id(UUID accountId, UUID topicId);

    @Query("SELECT up FROM UserProgress up WHERE up.account.id = :accountId AND up.topic.language.id = :languageId")
    List<UserProgress> findByAccount_IdAndTopic_Language_Id(@Param("accountId") UUID accountId, @Param("languageId") UUID languageId);

    boolean existsByAccountIdAndTopicId(UUID accountId, UUID topicId);
    @Query("SELECT DISTINCT up.account FROM UserProgress up WHERE up.account.notifReviewReminder = true AND up.completedLessons > 0 GROUP BY up.account HAVING MAX(up.updatedAt) < :startOfDay AND MAX(up.updatedAt) >= :threeDaysAgo")
    List<Accounts> findAccountsWithPendingReview(@Param("startOfDay") LocalDateTime startOfDay,@Param("threeDaysAgo") LocalDateTime threeDaysAgo);
}
