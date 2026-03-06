package com.glotrush.repositories;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glotrush.entities.Subscription;
import com.glotrush.enumerations.SubscriptionStatus;
import com.glotrush.enumerations.SubscriptionType;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    
    Optional<Subscription> findByAccount_Id(UUID accountId);
    boolean existsByAccount_Id(UUID accountId);
    List<Subscription> findAllBySubscriptionTypeAndIsActiveTrueAndEndDateBefore(SubscriptionType subscriptionType, LocalDateTime now);
    List<Subscription> findAllBySubscriptionTypeAndIsActiveTrueAndEndDateBetween(SubscriptionType subscriptionType, LocalDateTime start, LocalDateTime end);
    Optional<Subscription> findByStripeCustomerId(String stripeCustomerId);
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
    List<Subscription> findAllBySubscriptionTypeAndIsActiveTrueAndCurrentPeriodEndBetween(SubscriptionType subscriptionType,LocalDateTime start,LocalDateTime end);
    List<Subscription> findAllByStatusAndCancelAtPeriodEndTrue(SubscriptionStatus status);
    List<Subscription> findAllByStatusIn(List<SubscriptionStatus> statuses);
    List<Subscription> findAllByCurrentPeriodEndBeforeAndIsActiveTrue(LocalDateTime dateTime);
}
