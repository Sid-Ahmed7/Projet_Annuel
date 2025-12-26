package com.glotrush.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glotrush.entities.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    
}
