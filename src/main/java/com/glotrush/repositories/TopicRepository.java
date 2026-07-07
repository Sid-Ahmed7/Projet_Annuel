package com.glotrush.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.Topic;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID>, JpaSpecificationExecutor<Topic> {

    List<Topic> findByTargetLanguage_IdAndIsActiveTrueOrderByDifficultyAscNameAsc(UUID targetLanguageId);
    List<Topic> findByIsActiveTrueOrderByDifficultyAscNameAsc();
    List<Topic> findByTargetLanguage_Id(UUID targetLanguageId);

    @Query(
        value = """
            SELECT t FROM Topic t
            LEFT JOIN UserProgress up ON up.topic.id = t.id AND up.account.id = :accountId
            WHERE t.targetLanguage.id = :languageId
              AND t.isActive = true
              AND (up.completionPercentage IS NULL OR up.completionPercentage < 100)
            ORDER BY t.difficulty ASC, t.name ASC
            """,
        countQuery = """
            SELECT COUNT(t) FROM Topic t
            LEFT JOIN UserProgress up ON up.topic.id = t.id AND up.account.id = :accountId
            WHERE t.targetLanguage.id = :languageId
              AND t.isActive = true
              AND (up.completionPercentage IS NULL OR up.completionPercentage < 100)
            """
    )
    Page<Topic> findActiveTopicsWithProgress(
            @Param("languageId") UUID languageId,
            @Param("accountId") UUID accountId,
            Pageable pageable
    );

}
