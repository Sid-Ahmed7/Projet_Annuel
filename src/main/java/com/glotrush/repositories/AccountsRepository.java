package com.glotrush.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.Accounts;
import com.glotrush.enumerations.UserRole;

@Repository
public interface AccountsRepository extends JpaRepository<Accounts, UUID> {
    Optional<Accounts> findByEmail(String email);
    Optional<Accounts> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<Accounts> findByRole(UserRole role);

    
}
