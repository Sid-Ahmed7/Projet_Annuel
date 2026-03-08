package com.glotrush.repositories;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.glotrush.entities.Subscription;
import com.glotrush.enumerations.SubscriptionStatus;
import com.glotrush.enumerations.SubscriptionType;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByAccount_Id(UUID accountId);
    boolean existsByAccount_Id(UUID accountId);

    @Query("SELECT s FROM Subscription s WHERE s.plan.subscriptionType = :type AND s.isActive = true AND s.endDate < :now")
    List<Subscription> findAllByPlanSubscriptionTypeAndIsActiveTrueAndEndDateBefore(@Param("type") SubscriptionType type, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Subscription s WHERE s.plan.subscriptionType = :type AND s.isActive = true AND s.endDate BETWEEN :start AND :end")
    List<Subscription> findAllByPlanSubscriptionTypeAndIsActiveTrueAndEndDateBetween(@Param("type") SubscriptionType type, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    Optional<Subscription> findByStripeCustomerId(String stripeCustomerId);
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    @Query("SELECT s FROM Subscription s WHERE s.plan.subscriptionType = :type AND s.isActive = true AND s.currentPeriodEnd BETWEEN :start AND :end")
    List<Subscription> findAllByPlanSubscriptionTypeAndIsActiveTrueAndCurrentPeriodEndBetween(@Param("type") SubscriptionType type, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Subscription> findAllByStatusAndCancelAtPeriodEndTrue(SubscriptionStatus status);
    List<Subscription> findAllByStatusIn(List<SubscriptionStatus> statuses);
    List<Subscription> findAllByCurrentPeriodEndBeforeAndIsActiveTrue(LocalDateTime dateTime);
}
