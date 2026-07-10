package com.glotrush.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.glotrush.enumerations.LessonSessionStatus;
import com.glotrush.entities.LessonSession;

public interface LessonSessionRepository extends JpaRepository<LessonSession, UUID> {
    
    List<LessonSession> findByAccount_IdOrderByCompletedAtDesc(UUID accountId);
    
    @Query("SELECT COALESCE(SUM(ls.totalTime), 0) FROM LessonSession ls")
    Long sumTotalStudyTimeSeconds();

    @Query("SELECT COALESCE(SUM(ls.totalTime), 0) FROM LessonSession ls WHERE ls.account.id = :accountId")
    Long sumStudyTimeByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT COALESCE(SUM(ls.totalTime), 0) FROM LessonSession ls WHERE ls.account.id = :accountId AND ls.lesson.topic.id = :topicId")
    Long sumStudyTimeByAccountIdAndTopicId(@Param("accountId") UUID accountId, @Param("topicId") UUID topicId);

    @Query("SELECT COUNT(DISTINCT ls.account.id) FROM LessonSession ls WHERE ls.completedAt >= :since")
    Long countActiveUsersSince(@Param("since") LocalDateTime since);

    @Query("SELECT MAX(ls.completedAt) FROM LessonSession ls WHERE ls.account.id = :accountId AND ls.status = :status")
    LocalDateTime findLastActivityByAccountId(@Param("accountId") UUID accountId, @Param("status") LessonSessionStatus status);

    void deleteByAccount_Id(UUID accountId);
}
