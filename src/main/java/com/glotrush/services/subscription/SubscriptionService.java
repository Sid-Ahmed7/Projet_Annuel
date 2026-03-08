package com.glotrush.services.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.glotrush.builder.SubscriptionBuilder;
import com.glotrush.dto.response.SubscriptionResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Plan;
import com.glotrush.entities.Subscription;
import com.glotrush.enumerations.SubscriptionStatus;
import com.glotrush.enumerations.SubscriptionType;
import com.glotrush.exceptions.SubscriptionAlreadyExistException;
import com.glotrush.exceptions.SubscriptionNotFoundException;
import com.glotrush.repositories.PlanRepository;
import com.glotrush.repositories.SubscriptionRepository;
import com.glotrush.services.EmailService;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService implements ISubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionBuilder subscriptionBuilder;
    private final PlanRepository planRepository;
    private final MessageSource messageSource;  
    private final EmailService emailService;



    @Override
    @Transactional
    public void createSubscriptionForUser(Accounts account) {
        if(subscriptionRepository.existsByAccount_Id(account.getId())) {
            throw new SubscriptionAlreadyExistException(messageSource.getMessage("error.subscription.alreadyExist", null, LocaleUtils.getCurrentLocale()));
        }
        Plan freePlan = planRepository.findByPriceAndIsActiveTrue(BigDecimal.ZERO)
            .orElse(null);
        Subscription subscription = subscriptionBuilder.buildFreeSubscription(account, freePlan);
        subscriptionRepository.save(subscription);
    }

    @Override
    public SubscriptionResponse getSubscription(UUID accountId) {
        Subscription subscription = subscriptionRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new SubscriptionNotFoundException(messageSource.getMessage("error.subscription.notfound", null, LocaleUtils.getCurrentLocale())));

        return subscriptionBuilder.mapToSubscriptionResponse(subscription);
    }

    @Override
    @Transactional
    public void checkAndChangeExpiredSubscriptions() {
    
    List<Subscription> expiredSubscriptions = subscriptionRepository.findAllBySubscriptionTypeAndIsActiveTrueAndEndDateBefore(SubscriptionType.PREMIUM, LocalDateTime.now());

        if(expiredSubscriptions.isEmpty()) {
            return;
        }

        for (Subscription subscription : expiredSubscriptions) {
            expireSingleSubscription(subscription.getId());
        }
    }

    @Override
    public void expireSingleSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(messageSource.getMessage("error.subscription.notfound", null, LocaleUtils.getCurrentLocale())));

        if(subscription == null) {
            return;
        }

        if(subscription.getSubscriptionType() != SubscriptionType.PREMIUM || !subscription.getIsActive()) {
            return;
        }
          if (subscription.getEndDate() != null && subscription.getEndDate().isAfter(LocalDateTime.now())) {
            return;
        }

        Plan freePlan = planRepository.findByPriceAndIsActiveTrue(BigDecimal.ZERO).orElse(null);



        subscription.setSubscriptionType(SubscriptionType.FREE);
        subscription.setPlan(freePlan);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(null);
        subscription.setCurrentPeriodEnd(null);
        subscription.setIsActive(true);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStripeSubscriptionId(null);
        subscriptionRepository.save(subscription);
        sendEmailWhenSubscriptionChangedToFree(subscription);
    }

     @Override
        public void sendReminderEmailForExpiringSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(messageSource.getMessage("error.subscription.notfound", null, LocaleUtils.getCurrentLocale())));
            if(subscription.getEndDate() == null) {
                return;
            }
          sendExpirationEmail(subscription);
        }
    
    @Override
    @Transactional
    public void sendEmailWhenExpiringSoon() {
        List<Subscription> expiringSubscriptions = subscriptionRepository.findAllBySubscriptionTypeAndIsActiveTrueAndEndDateBetween(SubscriptionType.PREMIUM, LocalDateTime.now(), LocalDateTime.now().plusHours(24));
        if(expiringSubscriptions.isEmpty()) {
            return;
        }

        for(Subscription subscription : expiringSubscriptions) {
            sendExpirationEmail(subscription);
        }
    }

    private void sendExpirationEmail(Subscription subscription) {
        Accounts account = subscription.getAccount();
        long dayRemaining = ChronoUnit.DAYS.between(LocalDate.now(), subscription.getEndDate().toLocalDate());
        emailService.sendSubscriptionExpiredSoonEmail(account.getEmail(), account.getUsername(), dayRemaining);
    }
    private void sendEmailWhenSubscriptionTypeChangedInPremium(Subscription subscription) {
        Accounts account = subscription.getAccount();
        emailService.sendPremiumUpgratedEmail(account.getEmail(), account.getUsername(), subscription.getEndDate());
        
    }
        private void sendEmailWhenSubscriptionChangedToFree(Subscription subscription) {
        Accounts account = subscription.getAccount();
        emailService.sendSubscriptionExpiredEmail(account.getEmail(), account.getUsername());
    }
       

    
}
