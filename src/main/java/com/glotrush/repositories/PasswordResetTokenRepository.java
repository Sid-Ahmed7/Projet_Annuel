package com.glotrush.repositories;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.glotrush.entities.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.isRevoked = true WHERE t.account.id = :accountId")
    void invalidateAllUserTokens(UUID accountId);

      @Query("SELECT t FROM PasswordResetToken t " +
           "WHERE t.token = :token " +
           "AND t.expiresAt > :now " +
           "AND t.isRevoked = false")
    Optional<PasswordResetToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

}
