package com.glotrush.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glotrush.entities.LessonSession;

public interface LessonSessionRepository extends JpaRepository<LessonSession, UUID> {
    
    List<LessonSession> findByAccount_IdOrderByCompletedAtDesc(UUID accountId);
}
