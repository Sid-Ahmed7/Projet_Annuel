package com.glotrush.repositories;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


import com.glotrush.entities.Plan;
import com.glotrush.enumerations.PaymentInterval;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

    List<Plan> findAllByIsActiveTrueOrderByPriceAsc();
    List<Plan> findAllByPaymentIntervalAndIsActiveTrue(PaymentInterval paymentInterval);
    Optional<Plan> findByPriceAndIsActiveTrue(BigDecimal price);
}
