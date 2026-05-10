package com.glotrush.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glotrush.entities.challenge.ChallengeParticipant;

public interface ChallengeParticipantsRepository extends JpaRepository<ChallengeParticipant, UUID> {
    
}
