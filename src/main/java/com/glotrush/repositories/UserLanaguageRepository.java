package com.glotrush.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glotrush.entities.UserLanguage;

public interface UserLanaguageRepository extends JpaRepository<UserLanguage, UUID> {
    
}
