package com.glotrush.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.Topic;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID>, JpaSpecificationExecutor<Topic> {

    List<Topic> findByTargetLanguage_IdAndIsActiveTrueOrderByDifficultyAscNameAsc(UUID targetLanguageId);
    List<Topic> findByIsActiveTrueOrderByDifficultyAscNameAsc();
    List<Topic> findByTargetLanguage_Id(UUID targetLanguageId);
}
