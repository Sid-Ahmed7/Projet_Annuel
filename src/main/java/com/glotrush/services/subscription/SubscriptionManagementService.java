package com.glotrush.services.subscription;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.glotrush.builder.PaymentHistoryBuilder;
import com.glotrush.builder.SubscriptionBuilder;
import com.glotrush.dto.request.CancelSubscriptionRequest;
import com.glotrush.dto.request.ChangePlanRequest;
import com.glotrush.dto.request.SubscribeToPlanRequest;
import com.glotrush.dto.response.CheckoutStripeResponse;
import com.glotrush.dto.response.PaymentHistoryResponse;
import com.glotrush.dto.response.SubscriptionDetailResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Plan;
import com.glotrush.entities.Subscription;
import com.glotrush.enumerations.SubscriptionStatus;
import com.glotrush.enumerations.SubscriptionType;
import com.glotrush.exceptions.SubscriptionNotFoundException;
import com.glotrush.exceptions.SubscriptionOperationException;
import com.glotrush.repositories.PaymentHistoryRepository;
import com.glotrush.repositories.PlanRepository;
import com.glotrush.repositories.SubscriptionRepository;
import com.glotrush.scheduler.services.SubscriptionSchedulerService;
import com.glotrush.services.EmailService;
import com.glotrush.services.plan.IPlanService;
import com.glotrush.services.stripe.IStripService;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionManagementService implements ISubscriptionManagementService {
  
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PlanRepository planRepository;
    private final IPlanService planService;
    private final IStripService stripService;
    private final SubscriptionBuilder subscriptionBuilder;
    private final PaymentHistoryBuilder paymentHistoryBuilder;
    private final MessageSource messageSource;
    private final SubscriptionSchedulerService subscriptionSchedulerService;
    private final EmailService emailService;

    @Override
    @Transactional
    public List<SubscriptionDetailResponse> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream().map(subscriptionBuilder::mapToSubscriptionDetailResponse).toList();
    }


    @Override
    @Transactional
    public SubscriptionDetailResponse getSubscriptionDetail(UUID accountId) {
        Subscription subscription = getSubscriptionByAccountId(accountId);
        return subscriptionBuilder.mapToSubscriptionDetailResponse(subscription);
    }

    @Override
    @Transactional
    public CheckoutStripeResponse subscribeToPlan(UUID accountId, SubscribeToPlanRequest request) {
        Subscription subscription = getSubscriptionByAccountId(accountId);
        Plan plan = planService.getPlanById(request.getPlanId());

        if(subscription.getPlan().getSubscriptionType() == SubscriptionType.PREMIUM){
            throw new SubscriptionOperationException(messageSource.getMessage("error.subscription.already_premium", null, LocaleUtils.getCurrentLocale()));
        }

        if(plan.getStripePriceId() == null || plan.getStripePriceId().isBlank()) {
            throw new SubscriptionOperationException(messageSource.getMessage("error.plan.stripe.notconfigured", null, LocaleUtils.getCurrentLocale()));
        }

        Accounts account = subscription.getAccount();
        String customerId = subscription.getStripeCustomerId();
        if(customerId == null) {
            customerId = stripService.createCustomer(account.getEmail(), account.getUsername());
            subscription.setStripeCustomerId(customerId);
            subscriptionRepository.save(subscription);
        }

        String sessionUrl = stripService.createCheckoutSession(customerId, plan.getStripePriceId(), accountId.toString(), plan.getId().toString());
        String sessionId = sessionUrl.substring(sessionUrl.lastIndexOf('/') + 1);
        return CheckoutStripeResponse.builder()
                .checkoutUrl(sessionUrl)
                .sessionId(sessionId)
                .build();
}

    @Override
    @Transactional
    public CheckoutStripeResponse changeSubscriptionPlan(UUID accountId, ChangePlanRequest request) {
        Subscription subscription = getSubscriptionByAccountId(accountId);
        Plan newPlan = planService.getPlanById(request.getNewPlanId());

        if(subscription.getPlan() != null && subscription.getPlan().getId().equals(newPlan.getId())) {
            throw new SubscriptionOperationException(messageSource.getMessage("error.subscription.same_plan", null, LocaleUtils.getCurrentLocale()));
        }

        boolean isFreePlan = newPlan.getStripePriceId() == null || newPlan.getStripePriceId().isBlank();

        if(subscription.getPlan().getSubscriptionType() == SubscriptionType.FREE) {
            if(isFreePlan) {
                throw new SubscriptionOperationException(messageSource.getMessage("error.subscription.same_plan", null, LocaleUtils.getCurrentLocale()));
            }

            String customerId = subscription.getStripeCustomerId();

            if(customerId == null) {
                Accounts account = subscription.getAccount();
                customerId = stripService.createCustomer(account.getEmail(), account.getUsername());
                subscription.setStripeCustomerId(customerId);
                subscriptionRepository.save(subscription);
            }

            String sessionUrl = stripService.createCheckoutSession(customerId, newPlan.getStripePriceId(), accountId.toString(), newPlan.getId().toString());
            String sessionId = sessionUrl.substring(sessionUrl.lastIndexOf('/') + 1);
            return CheckoutStripeResponse.builder()
                    .checkoutUrl(sessionUrl)
                    .sessionId(sessionId)
                    .build();
        } else if(isFreePlan) {
            if(subscription.getStripeSubscriptionId() != null) {
                stripService.cancelSubscriptionAtPeriodEnd(subscription.getStripeSubscriptionId());
            }
            subscription.setCancelAtPeriodEnd(true);
            subscriptionRepository.save(subscription);
            return CheckoutStripeResponse.builder()
                    .message("Subscription will be downgraded to Free")
                    .build();
        } else {
            String stripeSubscriptionId = subscription.getStripeSubscriptionId();
            stripService.schedulePlanChange(stripeSubscriptionId, newPlan.getStripePriceId());
            return CheckoutStripeResponse.builder()
                        .message("Plan changed successfully")
                        .build();
    }

    }

    @Override
    @Transactional
    public SubscriptionDetailResponse cancelSubscription(UUID accountId, CancelSubscriptionRequest request) {
        Subscription subscription = getSubscriptionByAccountId(accountId);
        if(subscription.getPlan().getSubscriptionType() != SubscriptionType.PREMIUM) {
            throw new SubscriptionOperationException(messageSource.getMessage("error.subscription.notpremium", null, LocaleUtils.getCurrentLocale()));
        }

        if(Boolean.TRUE.equals(request.getIsCancelAtPeriodEnd())) {
            if(subscription.getStripeSubscriptionId() != null) {
                stripService.cancelSubscriptionAtPeriodEnd(subscription.getStripeSubscriptionId());
            } 

            subscription.setCancelAtPeriodEnd(true);
        } else {
            if(subscription.getStripeSubscriptionId() != null) {
                    stripService.cancelSubscription(subscription.getStripeSubscriptionId());
            }
                
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setCanceledAt(LocalDateTime.now());
            subscription.setEndDate(LocalDateTime.now());
            subscription.setIsActive(true);
            Plan freePlan = planRepository.findBySubscriptionTypeAndIsActiveTrue(SubscriptionType.FREE).orElseThrow(() -> new SubscriptionNotFoundException(messageSource.getMessage("error.plan.notfound", null, LocaleUtils.getCurrentLocale())));
            subscription.setPlan(freePlan);
            subscription.setStripeSubscriptionId(null);
        }

        if(request.getCancellationReason() != null) {
            subscription.setCancelReason(request.getCancellationReason());
        }
        subscriptionSchedulerService.cancelAllSchedulesForSubscription(subscription.getId());

        subscriptionRepository.save(subscription);
        Accounts account = subscription.getAccount();
        emailService.sendSubscriptionCancellationEmail(account.getEmail(), account.getUsername());
        return subscriptionBuilder.mapToSubscriptionDetailResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionDetailResponse reactivateSubscription(UUID accountId) {

        Subscription subscription = getSubscriptionByAccountId(accountId);

        if(!Boolean.TRUE.equals(subscription.getCancelAtPeriodEnd())) {
            throw new SubscriptionOperationException(messageSource.getMessage("error.subscription.not_scheduled_for_cancellation", null, LocaleUtils.getCurrentLocale()));
        }

        if (subscription.getStripeSubscriptionId() != null) {
            stripService.reactivateSubscription(subscription.getStripeSubscriptionId());
        }

        subscription.setCancelAtPeriodEnd(false);
        subscription.setCancelReason(null);
        subscriptionRepository.save(subscription);
        return subscriptionBuilder.mapToSubscriptionDetailResponse(subscription);
    }


    @Override
    public List<PaymentHistoryResponse> getPaymentHistory(UUID accountId) {
        return paymentHistoryBuilder.mapToResponseList(
            paymentHistoryRepository.findAllByAccount_IdOrderByCreatedAtDesc(accountId));
    }

    
    private Subscription getSubscriptionByAccountId(UUID accountId) {
        return subscriptionRepository.findByAccount_Id(accountId).orElseThrow(() -> new SubscriptionNotFoundException(messageSource.getMessage("error.subscription.notfound", null, LocaleUtils.getCurrentLocale())));
    }    
}
