package com.glotrush.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.UserLanguage;
import com.glotrush.enumerations.LanguageType;

@Repository
public interface UserLanguageRepository extends JpaRepository<UserLanguage, UUID> {
    
    List<UserLanguage> findByAccount_Id(UUID accountId);
    
    List<UserLanguage> findByAccount_IdAndLanguageType(UUID accountId, LanguageType languageType);
    
    Optional<UserLanguage> findByAccount_IdAndLanguage_Id(UUID accountId, UUID languageId);

    Optional<UserLanguage> findByIdAndAccount_Id(UUID id, UUID accountId);

    boolean existsByAccount_IdAndLanguage_Id(UUID accountId, UUID languageId);

    boolean existsByAccount_IdAndLanguage_IdAndLanguageType(UUID accountId, UUID languageId, LanguageType languageType);

    boolean existsByAccount_IdAndLanguageType(UUID accountId, LanguageType languageType);

    void deleteByAccount_IdAndLanguage_Id(UUID accountId, UUID languageId);

    Long countByAccount_IdAndLanguageType(UUID accountId, LanguageType languageType);

    @Query("SELECT ul.language.id FROM UserLanguage ul WHERE ul.languageType = :type GROUP BY ul.language.id ORDER BY COUNT(ul.id) DESC")
    List<UUID> findMostPopularLanguageIdsByLearnerCount(@Param ("type") LanguageType languageType, Pageable pageable);
}
