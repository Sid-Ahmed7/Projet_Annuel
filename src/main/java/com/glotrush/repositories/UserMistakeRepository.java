package com.glotrush.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glotrush.entities.UserMistake;

public interface UserMistakeRepository extends JpaRepository<UserMistake, UUID> {
    
}
