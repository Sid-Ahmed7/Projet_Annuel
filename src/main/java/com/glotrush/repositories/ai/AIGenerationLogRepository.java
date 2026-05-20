package com.glotrush.repositories.ai;

import com.glotrush.entities.ai.AIGenerationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AIGenerationLogRepository extends JpaRepository<AIGenerationLog, UUID> {
}
