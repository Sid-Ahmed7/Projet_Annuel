package com.glotrush.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.glotrush.entities.UserProfile;
import java.util.List;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    
    Optional<UserProfile> findByAccount_Id(UUID accountId);
    boolean existsByAccount_Id(UUID accountId);

}
