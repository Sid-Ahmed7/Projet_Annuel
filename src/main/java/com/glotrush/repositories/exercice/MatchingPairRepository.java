package com.glotrush.repositories.exercice;

import com.glotrush.entities.exercice.MatchingPairEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MatchingPairRepository extends JpaRepository<MatchingPairEntity, UUID> {
}
