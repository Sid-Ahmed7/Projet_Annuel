package com.glotrush.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.Accounts;
import com.glotrush.dto.request.AccountXpRequest;
import com.glotrush.entities.UserProgress;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {
    
    List<UserProgress> findByAccount_Id(UUID accountId);

    Optional<UserProgress> findByAccount_IdAndTopic_Id(UUID accountId, UUID topicId);

    @Query("SELECT up FROM UserProgress up WHERE up.account.id = :accountId AND up.topic.targetLanguage.id = :languageId")
    List<UserProgress> findByAccount_IdAndTopic_TargetLanguage_Id(@Param("accountId") UUID accountId, @Param("languageId") UUID languageId);

    boolean existsByAccountIdAndTopicId(UUID accountId, UUID topicId);
    @Query("SELECT DISTINCT up.account FROM UserProgress up WHERE up.account.notifReviewReminder = true AND up.completedLessons > 0 GROUP BY up.account HAVING MAX(up.updatedAt) < :startOfDay AND MAX(up.updatedAt) >= :threeDaysAgo")
    List<Accounts> findAccountsWithPendingReview(@Param("startOfDay") LocalDateTime startOfDay,@Param("threeDaysAgo") LocalDateTime threeDaysAgo);
    
    @Query("SELECT up.account.id as accountId, SUM(up.totalXP) as totalXP FROM UserProgress up GROUP BY up.account.id ORDER BY SUM(up.totalXP) DESC")
    List<AccountXpRequest> findGlobalRanking(Pageable pageable);

    @Query("SELECT up.account.id as accountId, SUM(up.totalXP) as totalXP FROM UserProgress up WHERE up.topic.targetLanguage.id = :languageId GROUP BY up.account.id ORDER BY SUM(up.totalXP) DESC")
    List<AccountXpRequest> findLanguageRanking(@Param("languageId") UUID languageId, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT up.account.id) FROM UserProgress up")
    Long countGlobalParticipants();

    @Query("SELECT COUNT(DISTINCT up.account.id) FROM UserProgress up WHERE up.topic.targetLanguage.id = :languageId")
    Long countLanguageParticipants(@Param("languageId") UUID languageId);

    @Query("SELECT COALESCE(SUM(up.totalXP), 0) FROM UserProgress up WHERE up.account.id = :accountId")
    Long getXpByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT COALESCE(SUM(up.totalXP), 0) FROM UserProgress up WHERE up.account.id = :accountId AND up.topic.targetLanguage.id = :languageId")
    Long getLanguageXpByAccountId(@Param("accountId") UUID accountId, @Param("languageId") UUID languageId);

    @Query(value = "SELECT CAST(COUNT(*) + 1 AS BIGINT) FROM (SELECT account_id, SUM(total_xp) as xp FROM user_progress GROUP BY account_id) ranked WHERE xp > (SELECT COALESCE(SUM(total_xp), 0) FROM user_progress WHERE account_id = :accountId)", nativeQuery = true)
    Long findGlobalRankByAccountId(@Param("accountId") UUID accountId);

    @Query(value = "SELECT CAST(COUNT(*) + 1 AS BIGINT) FROM (SELECT up.account_id, SUM(up.total_xp) as xp FROM user_progress up JOIN topic t ON t.id = up.topic_id " +
                   "WHERE t.target_language_id = :languageId GROUP BY up.account_id) ranked WHERE xp > (SELECT COALESCE(SUM(up2.total_xp), 0) FROM user_progress up2 " +
                   "JOIN topic t2 ON t2.id = up2.topic_id WHERE up2.account_id = :accountId AND t2.target_language_id = :languageId)", nativeQuery = true)
    Long findLanguageRankByAccountId(@Param("accountId") UUID accountId, @Param("languageId") UUID languageId);

    @Query("SELECT COALESCE(AVG(up.accuracy), 0) FROM UserProgress up WHERE up.totalAnswers > 0")
    Double avgAccuracyGlobal();

    @Query("SELECT COALESCE(AVG(up.completionPercentage), 0) FROM UserProgress up")
    Double avgCompletionPercentageGlobal();

    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.account.id = :accountId AND up.completionPercentage >= 100")
    Long countCompletedTopicsByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT up.topic.targetLanguage.name FROM UserProgress up GROUP BY up.topic.targetLanguage.name ORDER BY SUM(up.totalXP) DESC")
    List<String> findMostStudiedLanguages(Pageable pageable);

    @Query("SELECT COALESCE(AVG(up.accuracy), 0) FROM UserProgress up WHERE up.account.id = :accountId AND up.totalAnswers > 0")
    Double avgAccuracyByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT COALESCE(SUM(up.completedLessons), 0) FROM UserProgress up WHERE up.account.id = :accountId")
    Long sumCompletedLessonsByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT up.account.id as accountId, SUM(up.totalXP) as totalXP FROM UserProgress up WHERE up.account.id IN :friendIds GROUP BY up.account.id ORDER BY SUM(up.totalXP) DESC")
    List<AccountXpRequest> findFriendsRanking(@Param("friendIds") List<UUID> friendIds, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT up.account.id) FROM UserProgress up WHERE up.account.id IN :friendIds")
    Long countFriendsParticipants(@Param("friendIds") List<UUID> friendIds);

    @Query(value = "SELECT CAST(COUNT(*) + 1 AS BIGINT) FROM (SELECT account_id, SUM(total_xp) AS xp FROM user_progress WHERE account_id IN (:friendIds) GROUP BY account_id) ranked " +
                   "WHERE xp > (SELECT COALESCE(SUM(total_xp), 0) FROM user_progress WHERE account_id = :accountId)", nativeQuery = true)
    Long findFriendsRankByAccountId(@Param("accountId") UUID accountId, @Param("friendIds") List<UUID> friendIds);

}
