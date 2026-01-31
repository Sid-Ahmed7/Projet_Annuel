package com.glotrush.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glotrush.entities.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    
}
