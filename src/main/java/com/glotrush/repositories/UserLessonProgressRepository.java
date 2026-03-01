package com.glotrush.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Query("SELECT COALESCE(SUM(ulp.timeSpentSeconds), 0L) FROM UserLessonProgress ulp WHERE ulp.account.id = :accountId")
    Long sumTimeSpentSecondsByAccount_Id(@Param("accountId") UUID accountId);

}
