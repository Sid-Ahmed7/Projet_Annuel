package com.glotrush.services.stripe;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.glotrush.entities.PaymentHistory;
import com.glotrush.entities.Plan;
import com.glotrush.entities.Subscription;
import com.glotrush.enumerations.PaymentInterval;
import com.glotrush.enumerations.PaymentStatus;
import com.glotrush.enumerations.SubscriptionStatus;
import com.glotrush.enumerations.SubscriptionType;
import com.glotrush.exceptions.SubscriptionNotFoundException;
import com.glotrush.repositories.PaymentHistoryRepository;
import com.glotrush.repositories.PlanRepository;
import com.glotrush.repositories.SubscriptionRepository;
import com.glotrush.scheduler.services.SubscriptionSchedulerService;
import com.stripe.model.checkout.Session;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PlanRepository planRepository;
    private final SubscriptionSchedulerService subscriptionSchedulerService;


    @Transactional
    public void finalizeCheckout(Session session) {
        String accountId = session.getMetadata().get("account_id");
        String planId = session.getMetadata().get("plan_id");
        String stripeSubscriptionId = session.getSubscription();
        String stripeCustomerId = session.getCustomer();

        if (accountId == null ||planId == null) {
           log.warn("Checkout session {} missing metadata", session.getId());
            return;
        }

        Subscription subscription = subscriptionRepository.findByAccount_Id(UUID.fromString(accountId))
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found for account: " + accountId));

        if(subscription.getStripeSubscriptionId() != null && subscription.getStripeSubscriptionId().equals(stripeSubscriptionId)) {
            return;
        }

        Plan plan = planRepository.findById(UUID.fromString(planId)).orElse(null);
        if (plan == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodEnd = calculatePeriodEnd(now, plan.getPaymentInterval());

        subscription.setSubscriptionType(SubscriptionType.PREMIUM);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setIsActive(true);
        subscription.setStartDate(now);
        subscription.setEndDate(periodEnd);
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(periodEnd);
        subscription.setCancelAtPeriodEnd(false);
        subscription.setCanceledAt(null);
        subscription.setCancelReason(null);
        subscription.setStripeSubscriptionId(stripeSubscriptionId);
        subscription.setStripeCustomerId(stripeCustomerId);

        subscriptionRepository.save(subscription);
        subscriptionSchedulerService.scheduleExpiration(subscription);
        subscriptionSchedulerService.scheduleReminder(subscription);
        
        BigDecimal amount = BigDecimal.valueOf(session.getAmountTotal())
                .divide(BigDecimal.valueOf(100));

        PaymentHistory payment = PaymentHistory.builder()
                .account(subscription.getAccount())
                .subscription(subscription)
                .transactionId(session.getPaymentIntent())
                .amount(amount)
                .currency(session.getCurrency().toUpperCase())
                .paymentStatus(PaymentStatus.SUCCEEDED)
                .paymentMethod("stripe")
                .paymentInterval(plan.getPaymentInterval())
                .paymentAt(now)
                .build();

        paymentHistoryRepository.save(payment);

    }
        @Transactional
        public void handleSubscriptionDeleted(com.stripe.model.Subscription stripeSubscription) {
        Subscription subscription = subscriptionRepository
                .findByStripeSubscriptionId(stripeSubscription.getId()).orElse(null);
        if (subscription == null) return;

        subscriptionSchedulerService.cancelAllSchedulesForSubscription(subscription.getId());

        Plan freePlan = planRepository.findByPriceAndIsActiveTrue(BigDecimal.ZERO).orElse(null);

        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscription.setSubscriptionType(SubscriptionType.FREE);
        subscription.setPlan(freePlan);
        subscription.setIsActive(true);
        subscription.setCanceledAt(LocalDateTime.now());
        subscription.setStripeSubscriptionId(null);
        subscription.setCurrentPeriodEnd(null);

        subscriptionRepository.save(subscription);
        }


    private LocalDateTime calculatePeriodEnd(LocalDateTime start, PaymentInterval interval) {
        return switch (interval) {
            case MONTHLY -> start.plusMonths(1);
            case YEARLY -> start.plusYears(1);
        };
    }

    
    
}
