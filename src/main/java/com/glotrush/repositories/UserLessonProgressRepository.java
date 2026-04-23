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
import com.glotrush.entities.UserLessonProgress;
import com.glotrush.enumerations.LessonStatus;

@Repository
public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, UUID> {

    Optional<UserLessonProgress> findByAccount_IdAndLesson_Id(UUID accountId, UUID lessonId);

    List<UserLessonProgress> findByAccount_Id(UUID accountId);

    List<UserLessonProgress> findByAccount_IdAndStatus(UUID accountId, LessonStatus status);

    Integer countByAccountIdAndStatus(UUID accountId, LessonStatus status);

    boolean existsByAccountIdAndLessonId(UUID accountId, UUID lessonId);

    List<UserLessonProgress> findByAccount_IdAndLesson_Topic_Id(UUID accountId, UUID topicId);

    @Query("SELECT ulp FROM UserLessonProgress ulp WHERE ulp.account.id = :accountId AND ulp.lastAttemptAt IS NOT NULL ORDER BY ulp.lastAttemptAt DESC LIMIT 1")
    Optional<UserLessonProgress> findLastAttemptByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT COUNT(ulp) FROM UserLessonProgress ulp WHERE ulp.account.id = :accountId AND ulp.lesson.topic.id = :topicId AND ulp.status = 'COMPLETED'")
    Integer countCompletedByAccountAndTopic(@Param("accountId") UUID accountId, @Param("topicId") UUID topicId);

    @Query("SELECT ulp FROM UserLessonProgress ulp WHERE ulp.account.id = :accountId AND ulp.lesson.topic.id = :topicId AND ulp.status != 'COMPLETED' ORDER BY ulp.lesson.orderIndex ASC")
    Optional<UserLessonProgress> findFirstUncompletedINTopic(@Param("accountId") UUID accountId, @Param("topicId") UUID topicId);


    @Query("SELECT a FROM Accounts a WHERE a.notifLessonReminder = true AND a.id NOT IN (SELECT DISTINCT ulp.account.id FROM UserLessonProgress ulp WHERE ulp.completedAt >= :startOfDay)")
    List<Accounts> findAccountsWithNoLessonToday(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT DISTINCT ulp.account FROM UserLessonProgress ulp WHERE ulp.account.notifStreakUrgency = true AND ulp.account.id NOT IN (SELECT DISTINCT ulp2.account.id FROM UserLessonProgress ulp2 WHERE ulp2.completedAt >= :startOfDay)")
    List<Accounts> findAccountsWithNoLessonTodayForStreak(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT DISTINCT ulp.account FROM UserLessonProgress ulp WHERE ulp.account.notifInactivity = true GROUP BY ulp.account HAVING MAX(ulp.completedAt) < :recentLimit AND MAX(ulp.completedAt) >= :oldLimit")
    List<Accounts> findInactiveAccounts(@Param("recentLimit") LocalDateTime recentLimit, @Param("oldLimit") LocalDateTime oldLimit);

    @Query("SELECT ulp.account, COUNT(ulp) FROM UserLessonProgress ulp WHERE ulp.completedAt >= :startOfWeek AND ulp.status = 'COMPLETED' AND ulp.account.notifWeeklyGoal = true GROUP BY ulp.account HAVING COUNT(ulp) >= :goal")
    List<Object[]> findAccountsWhoReachedWeeklyGoal(@Param("startOfWeek") LocalDateTime startOfWeek,@Param("goal") int goal);




}


