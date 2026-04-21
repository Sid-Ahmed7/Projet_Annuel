package com.glotrush.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("SELECT ulp FROM UserLessonProgress ulp WHERE ulp.account.id = :accountId AND ulp.lastAttemptAt IS NOT NULL ORDER BY ulp.lastAttemptAt DESC")
    List<UserLessonProgress> findLastAttempts(@Param("accountId") UUID accountId, Pageable pageable);

    default Optional<UserLessonProgress> findLastAttemptByAccountId(UUID accountId) {
        return findLastAttempts(accountId, PageRequest.of(0, 1)).stream().findFirst();
    }

    @Query("SELECT COUNT(ulp) FROM UserLessonProgress ulp WHERE ulp.account.id = :accountId AND ulp.lesson.topic.id = :topicId AND ulp.status = 'COMPLETED'")
    Integer countCompletedByAccountAndTopic(@Param("accountId") UUID accountId, @Param("topicId") UUID topicId);

    @Query("SELECT ulp FROM UserLessonProgress ulp WHERE ulp.account.id = :accountId AND ulp.lesson.topic.id = :topicId AND ulp.status != 'COMPLETED' ORDER BY ulp.lesson.orderIndex ASC")
    List<UserLessonProgress> findUncompletedInTopic(@Param("accountId") UUID accountId, @Param("topicId") UUID topicId, Pageable pageable);

    default Optional<UserLessonProgress> findFirstUncompletedINTopic(UUID accountId, UUID topicId) {
        return findUncompletedInTopic(accountId, topicId, PageRequest.of(0, 1)).stream().findFirst();
    }
}
