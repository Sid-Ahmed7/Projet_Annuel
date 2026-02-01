package com.glotrush.services.subscription;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.glotrush.builder.SubscriptionBuilder;
import com.glotrush.dto.request.ChangeSubscriptionRequest;
import com.glotrush.dto.response.SubscriptionResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Subscription;
import com.glotrush.enumerations.SubscriptionType;
import com.glotrush.exceptions.SubscriptionAlreadyExistException;
import com.glotrush.exceptions.SubscriptionNotFoundException;
import com.glotrush.repositories.SubscriptionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService implements ISubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionBuilder subscriptionBuilder;
    private final MessageSource messageSource;  

    private static final int PREMIUM_DAYS_SUBSCRIPTION = 50;


    protected final Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }
    @Override
    @Transactional
    public void createSubscriptionForUser(Accounts account) {
        if(subscriptionRepository.existsByAccount_Id(account.getId())) {
            throw new SubscriptionAlreadyExistException(messageSource.getMessage("error.subscription.alreadyExist", null, getCurrentLocale()));
        }

        Subscription subscription = subscriptionBuilder.buildFreeSubscription(account);
        subscriptionRepository.save(subscription);
    }

    @Override
    public SubscriptionResponse getSubscription(UUID accountId) {
        Subscription subscription = subscriptionRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new SubscriptionNotFoundException(messageSource.getMessage("error.subscription.notfound", null, getCurrentLocale())));

        return subscriptionBuilder.mapToSubscriptionResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse changeSubscriptionType(UUID accountId, ChangeSubscriptionRequest subscriptionType) {
        Subscription subscription = subscriptionRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new SubscriptionNotFoundException(messageSource.getMessage("error.subscription.notFound",null, getCurrentLocale())));

        subscription.setSubscriptionType(subscriptionType.getSubscriptionType());
        
        if(subscriptionType.getSubscriptionType() == SubscriptionType.PREMIUM) {
            subscription.setStartDate(LocalDateTime.now());
            subscription.setEndDate(LocalDateTime.now().plusDays(PREMIUM_DAYS_SUBSCRIPTION));
            subscription.setIsActive(true);
        } else {
            subscription.setStartDate(LocalDateTime.now());
            subscription.setEndDate(null);
            subscription.setIsActive(true);
        }

        return subscriptionBuilder.mapToSubscriptionResponse(subscription);
    }
    
}
