package com.glotrush.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.glotrush.entities.PushNotificationSubscription;

import org.springframework.transaction.annotation.Transactional;

public interface PushNotificationSubscriptionRepository extends JpaRepository<PushNotificationSubscription, UUID> {

    List<PushNotificationSubscription> findByAccount_Id(UUID accountId);
    Optional<PushNotificationSubscription> findByEndpoint(String endpoint);
    @Transactional
    @Modifying
    void deleteByEndpoint(String endpoint);
    boolean existsByEndpoint(String endpoint);
    @Transactional
    @Modifying
    void deleteByEndpointAndAccount_Id(String endpoint, UUID accountId);
    
}
