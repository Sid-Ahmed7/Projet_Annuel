package com.glotrush.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.Lesson;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    @EntityGraph(attributePaths = {"topic"})
    List<Lesson> findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(UUID topicId);

    @EntityGraph(attributePaths = {"topic"})
    List<Lesson> findByTopic_Id(UUID topicId);

    @EntityGraph(attributePaths = {"topic"})
    List<Lesson> findByTopic_IdOrderByOrderIndexAsc(UUID topicId);

    @EntityGraph(attributePaths = {"topic"})
    Optional<Lesson> findById(UUID id);

    Integer countByTopic_Id(UUID topicId);
    Integer countByTopic_Language_Id(UUID languageId);
    boolean existsById(UUID lessonId);
    Optional<Lesson> findFirstByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(UUID topicId);

    @Query("SELECT l FROM Lesson l " +
           "LEFT JOIN UserLessonProgress ulp ON ulp.lesson.id = l.id AND ulp.account.id = :accountId " +
           "WHERE l.topic.id = :topicId AND l.isActive = true " +
           "AND (ulp.id IS NULL OR ulp.status != 'COMPLETED') " +
           "ORDER BY l.orderIndex ASC")
    Optional<Lesson> findFirstUncompletedLessonInTopic(@Param("accountId") UUID accountId, @Param("topicId") UUID topicId);

}
