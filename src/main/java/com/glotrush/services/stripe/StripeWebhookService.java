package com.glotrush.services.stripe;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.glotrush.entities.Accounts;
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
import com.glotrush.services.EmailService;
import com.glotrush.utils.LocaleUtils;
import com.stripe.model.Invoice;
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
    private final MessageSource messageSource;
    private final EmailService emailService;


    @Transactional
    public void finalizeCheckout(Session session) {
        String accountId = session.getMetadata().get("account_id");
        String planId = session.getMetadata().get("plan_id");
        String stripeSubscriptionId = session.getSubscription();
        String stripeCustomerId = session.getCustomer();

        if (accountId == null ||planId == null) {
            return;
        }

        Subscription subscription = subscriptionRepository.findByAccount_Id(UUID.fromString(accountId))
                .orElseThrow(() -> new SubscriptionNotFoundException(messageSource.getMessage("error.subscription.notfound", new Object[]{accountId}, LocaleUtils.getCurrentLocale())));

        if(subscription.getStripeSubscriptionId() != null && subscription.getStripeSubscriptionId().equals(stripeSubscriptionId)) {
            return;
        }

        Plan plan = planRepository.findById(UUID.fromString(planId)).orElse(null);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodEnd = calculatePeriodEnd(now, plan.getPaymentInterval());

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
        
        BigDecimal amount = BigDecimal.valueOf(session.getAmountTotal()).divide(BigDecimal.valueOf(100));

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
        Accounts account = subscription.getAccount();
        emailService.sendPremiumUpgratedEmail(account.getEmail(), account.getUsername(), periodEnd);

    }
        @Transactional
        public void handleSubscriptionDeleted(com.stripe.model.Subscription stripeSubscription) {
        Subscription subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSubscription.getId()).orElseThrow(() -> new SubscriptionNotFoundException(messageSource.getMessage("error.subscription.notfound.stripe", new Object[]{stripeSubscription.getId()}, LocaleUtils.getCurrentLocale())));
        if (subscription == null){
            return;
        }
        subscriptionSchedulerService.cancelAllSchedulesForSubscription(subscription.getId());

        Plan freePlan = planRepository.findBySubscriptionTypeAndIsActiveTrue(SubscriptionType.FREE).orElse(null);

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setPlan(freePlan);
        subscription.setIsActive(true);
        subscription.setCanceledAt(LocalDateTime.now());
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(null);
        subscription.setStripeSubscriptionId(null);
        subscription.setCurrentPeriodEnd(null);

        subscriptionRepository.save(subscription);
        }

        @Transactional
        public void renewSubscription(Invoice invoice) {
            Subscription subscription = subscriptionRepository.findByStripeSubscriptionId(invoice.getSubscription()).orElse(null);
            if (subscription == null){
                return;
            }
            subscription.setCurrentPeriodStart(LocalDateTime.now());
            subscription.setCurrentPeriodEnd(calculatePeriodEnd(LocalDateTime.now(), subscription.getPlan().getPaymentInterval()));
            subscription.setEndDate(calculatePeriodEnd(LocalDateTime.now(), subscription.getPlan().getPaymentInterval()));

            subscriptionRepository.save(subscription);
            subscriptionSchedulerService.cancelAllSchedulesForSubscription(subscription.getId());
            subscriptionSchedulerService.scheduleExpiration(subscription);
            subscriptionSchedulerService.scheduleReminder(subscription);

            BigDecimal amount = BigDecimal.valueOf(invoice.getAmountPaid()).divide(BigDecimal.valueOf(100));
            
            PaymentHistory payment = PaymentHistory.builder()
                    .account(subscription.getAccount())
                    .subscription(subscription)
                    .transactionId(invoice.getPaymentIntent())
                    .amount(amount)
                    .currency(invoice.getCurrency().toUpperCase())
                    .paymentStatus(PaymentStatus.SUCCEEDED)
                    .paymentMethod("stripe")
                    .paymentInterval(subscription.getPlan().getPaymentInterval())
                    .paymentAt(LocalDateTime.now())
                    .build();
        
            paymentHistoryRepository.save(payment);
        
        }


    private LocalDateTime calculatePeriodEnd(LocalDateTime start, PaymentInterval interval) {
        return switch (interval) {
            case MONTHLY -> start.plusMonths(1);
            case YEARLY -> start.plusYears(1);
        };
    }

    
    
}
