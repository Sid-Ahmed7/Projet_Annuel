package com.glotrush.builder;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.glotrush.dto.response.SubscriptionResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Subscription;
import com.glotrush.enumerations.SubscriptionType;

@Component
public class SubscriptionBuilder {
    
    public Subscription buildFreeSubscription(Accounts account) {
        return Subscription.builder()
                .account(account)
                .subscriptionType(SubscriptionType.FREE)
                .startDate(LocalDateTime.now())
                .endDate(null)
                .isActive(true)
                .build();
    }

    public Subscription buildPremiumSubscription(Accounts account) {
        return Subscription.builder()
                .account(account)
                .subscriptionType(SubscriptionType.PREMIUM)
                .startDate(LocalDateTime.now())
                .endDate(null)
                .isActive(true)
                .build();
    }

    public SubscriptionResponse mapToSubscriptionResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .subscriptionType(subscription.getSubscriptionType().name())
                .isActive(subscription.getIsActive())
                .startDate(subscription.getStartDate().toString())
                .endDate(subscription.getEndDate() != null ? subscription.getEndDate().toString() : null)
                .build();
    }
}
