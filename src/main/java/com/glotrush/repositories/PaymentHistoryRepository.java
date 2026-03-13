package com.glotrush.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.glotrush.entities.PaymentHistory;
import com.glotrush.enumerations.PaymentStatus;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, UUID> {

    List<PaymentHistory> findAllByAccount_IdOrderByCreatedAtDesc(UUID accountId);

    Page<PaymentHistory> findAllByAccount_IdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);
  Optional<PaymentHistory> findByTransactionId(String transactionId);

    List<PaymentHistory> findAllByAccount_IdAndPaymentStatus(UUID accountId, PaymentStatus status);

    List<PaymentHistory> findAllByAccount_IdAndCreatedAtBetween(UUID accountId,LocalDateTime startDate,LocalDateTime endDate);

    List<PaymentHistory> findAllBySubscription_Id(UUID subscriptionId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentHistory p WHERE p.paymentStatus = 'SUCCEEDED'")
    BigDecimal calculateTotalRevenue();
}
