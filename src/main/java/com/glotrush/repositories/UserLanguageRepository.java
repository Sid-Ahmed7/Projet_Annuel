package com.glotrush.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.UserLanguage;
import com.glotrush.enumerations.LanguageType;

@Repository
public interface UserLanguageRepository extends JpaRepository<UserLanguage, UUID> {
    
    List<UserLanguage> findByAccount_Id(UUID accountId);
    
    List<UserLanguage> findByAccount_IdAndLanguageType(UUID accountId, LanguageType languageType);
    
    Optional<UserLanguage> findByAccount_IdAndLanguage_Id(UUID accountId, UUID languageId);

    boolean existsByAccount_IdAndLanguage_Id(UUID accountId, UUID languageId);

    void deleteByAccount_IdAndLanguage_Id(UUID accountId, UUID languageId);

}
