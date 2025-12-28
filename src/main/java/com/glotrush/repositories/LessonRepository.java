package com.glotrush.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.Lesson;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    
}
