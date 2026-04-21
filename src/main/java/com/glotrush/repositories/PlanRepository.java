package com.glotrush.repositories;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.glotrush.entities.Plan;
import com.glotrush.enumerations.PaymentInterval;
import com.glotrush.enumerations.SubscriptionType;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

    List<Plan> findAllByIsActiveTrueOrderByPriceAsc();
    List<Plan> findAllByOrderByPriceAsc();
    List<Plan> findAllByPaymentIntervalAndIsActiveTrue(PaymentInterval paymentInterval);
    Optional<Plan> findBySubscriptionTypeAndIsActiveTrue(SubscriptionType subscriptionType);
}
