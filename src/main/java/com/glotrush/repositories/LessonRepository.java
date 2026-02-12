package com.glotrush.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.Lesson;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    
    List<Lesson> findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(UUID topicId);
    List<Lesson> findByTopic_Id(UUID topicId);
    Integer countByTopic_Id(UUID topicId);
    boolean existsById(UUID LessonId);


}
