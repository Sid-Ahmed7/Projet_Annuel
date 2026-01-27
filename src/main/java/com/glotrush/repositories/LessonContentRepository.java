package com.glotrush.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.LessonContent;

@Repository
public interface LessonContentRepository extends JpaRepository<LessonContent, UUID> {
    
    Optional<LessonContent> findByLesson_Id(UUID lessonId);

    Optional<LessonContent> findByLesson_IdAndLanguageCode(UUID lessonId, String languageCode);



}
