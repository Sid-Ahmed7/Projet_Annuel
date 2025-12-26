package com.glotrush.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glotrush.entities.Language;

public interface LanguageRepository extends JpaRepository<Language, UUID> {
    
}
