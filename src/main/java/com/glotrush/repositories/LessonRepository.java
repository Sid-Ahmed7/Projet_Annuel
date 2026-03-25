package com.glotrush.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.Lesson;

import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    @EntityGraph(attributePaths = {"topic"})
    List<Lesson> findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(UUID topicId);

    @EntityGraph(attributePaths = {"topic"})
    List<Lesson> findByTopic_Id(UUID topicId);

    @EntityGraph(attributePaths = {"topic"})
    Optional<Lesson> findById(UUID id);

    Integer countByTopic_Id(UUID topicId);
    Integer countByTopic_Language_Id(UUID languageId);
    boolean existsById(UUID LessonId);
}
