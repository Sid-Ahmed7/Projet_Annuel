package com.glotrush.builder;

import java.util.List;

import org.springframework.stereotype.Component;

import com.glotrush.dto.response.PlanFeatureResponse;
import com.glotrush.dto.response.PlanResponse;
import com.glotrush.entities.Plan;

@Component
public class PlanBuilder {

    public PlanResponse mapToResponse(Plan plan) {
        if(plan == null) {
            return null;
        }

        List<PlanFeatureResponse> benefits = plan.getFeatures().stream()
                .map(feature -> PlanFeatureResponse.builder()
                    .id(feature.getId())
                    .label(feature.getLabel())
                    .orderIndex(feature.getOrderIndex())
                    .build())
                .toList();

        return PlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .paymentInterval(plan.getPaymentInterval())
                .subscriptionType(plan.getSubscriptionType())
                .isActive(plan.getIsActive())
                .aiQuota(plan.getAiQuota())
                .features(benefits)
                .build();
    }
    
}
