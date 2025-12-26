package com.glotrush.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glotrush.entities.TwoFactorAuth;

public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, UUID>{
    boolean existsByAccount_IdAndActiveTrue(UUID accountId);

    Optional<TwoFactorAuth> findFirstByAccount_IdAndActiveTrue(UUID accountId);

}
