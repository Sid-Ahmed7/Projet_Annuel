package com.glotrush.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.Topic;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {
    
}
