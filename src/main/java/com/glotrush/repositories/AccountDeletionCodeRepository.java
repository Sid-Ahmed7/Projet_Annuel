package com.glotrush.repositories;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.glotrush.entities.AccountDeletionCode;

public interface AccountDeletionCodeRepository extends JpaRepository<AccountDeletionCode, UUID> {

    @Modifying
    @Query("UPDATE AccountDeletionCode c SET c.isRevoked = true WHERE c.account.id = :accountId")
    void revokeAllByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT c FROM AccountDeletionCode c WHERE c.code = :code AND c.expiresAt > :now AND c.isUsed = false AND c.isRevoked = false")
    Optional<AccountDeletionCode> findValidCode(@Param("code") String code, @Param("now") LocalDateTime now);

    void deleteByAccount_Id(UUID accountId);
}