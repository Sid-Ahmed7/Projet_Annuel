package com.glotrush.repositories.exercice;

import com.glotrush.entities.exercice.InteractiveQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InteractiveQuestionRepository extends JpaRepository<InteractiveQuestionEntity, UUID> {
}
