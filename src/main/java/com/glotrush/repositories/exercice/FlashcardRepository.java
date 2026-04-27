package com.glotrush.repositories.exercice;

import com.glotrush.entities.exercice.FlashcardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FlashcardRepository extends JpaRepository<FlashcardEntity, UUID> {
}
