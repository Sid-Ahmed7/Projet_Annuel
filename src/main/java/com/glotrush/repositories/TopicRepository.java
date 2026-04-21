package com.glotrush.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.Topic;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID>, JpaSpecificationExecutor<Topic> {

    List<Topic> findByLanguage_IdAndIsActiveTrueOrderByDifficultyAscNameAsc(UUID languageId);
    List<Topic> findByIsActiveTrueOrderByDifficultyAscNameAsc();
    List<Topic> findByLanguage_Id(UUID languageId);
}
