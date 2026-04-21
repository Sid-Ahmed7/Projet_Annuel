package com.glotrush.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.Language;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

@Repository
public interface LanguageRepository extends JpaRepository<Language, UUID> {

    Optional<Language> findByCode(String code);

    List<Language> findByIsActiveTrueOrderByNameAsc();
    
    boolean existsByCode(String code);
}
