package com.glotrush.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glotrush.entities.challenge.Challenge;

public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {

} 
