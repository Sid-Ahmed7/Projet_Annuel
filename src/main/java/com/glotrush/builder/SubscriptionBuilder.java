package com.glotrush.builder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.glotrush.dto.response.SubscriptionDetailResponse;
import com.glotrush.dto.response.SubscriptionResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Plan;
import com.glotrush.entities.Subscription;
import com.glotrush.enumerations.SubscriptionType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionBuilder {

    private final PlanBuilder planBuilder;
    
    public Subscription buildFreeSubscription(Accounts account, Plan freePlan) {
        return Subscription.builder()
                .account(account)
                .plan(freePlan)
                .subscriptionType(SubscriptionType.FREE)
                .startDate(LocalDateTime.now())
                .endDate(null)
                .currentPeriodStart(LocalDateTime.now())
                .currentPeriodEnd(null)
                .isActive(true)
                .cancelAtPeriodEnd(false)
                .build();
    }

 
    public SubscriptionResponse mapToSubscriptionResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .subscriptionType(subscription.getSubscriptionType())
                .isActive(subscription.getIsActive())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate() != null ? subscription.getEndDate() : null)
                .build();
    }

    public SubscriptionDetailResponse mapToSubscriptionDetailResponse(Subscription subscription) {
        
        if (subscription == null) {
            return null;
        }

        Long daysRemaining = null;
        if(subscription.getCurrentPeriodEnd() != null) {
            daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getCurrentPeriodEnd());
            if (daysRemaining < 0) {
                daysRemaining = 0L; 
            }
        }
        

        return SubscriptionDetailResponse.builder()
                .id(subscription.getId())
                .accountId(subscription.getAccount().getId())
                .firstName(subscription.getAccount().getFirstName())
                .lastName(subscription.getAccount().getLastName())
                .subscriptionType(subscription.getSubscriptionType())
                .status(subscription.getStatus())
                .plan(subscription.getPlan() != null ? planBuilder.mapToResponse(subscription.getPlan()) : null)
                .isActive(subscription.getIsActive())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate() != null ? subscription.getEndDate() : null)
                .currentPeriodStart(subscription.getCurrentPeriodStart())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .cancelAtPeriodEnd(subscription.getCancelAtPeriodEnd())
                .canceledAt(subscription.getCanceledAt())
                .daysRemaining(daysRemaining)
                .build();
    }
}

