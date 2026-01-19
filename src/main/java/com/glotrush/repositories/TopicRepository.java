package com.glotrush.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.Topic;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {

    List<Topic> findByLanguage_IdAndIsActiveTrueOrderByOrderIndexAsc(UUID languageId);
    List<Topic> findByIsActiveTrueOrderByOrderIndexAsc();
    List<Topic> findByLanguage_Id(UUID languageId);
}
