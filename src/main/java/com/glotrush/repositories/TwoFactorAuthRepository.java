package com.glotrush.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.TwoFactorAuth;

@Repository
public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, UUID>{
    boolean existsByAccount_IdAndActiveTrue(UUID accountId);

    Optional<TwoFactorAuth> findFirstByAccount_IdAndActiveTrue(UUID accountId);

}
