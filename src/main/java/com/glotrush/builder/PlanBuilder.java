package com.glotrush.builder;

import org.springframework.stereotype.Component;

import com.glotrush.dto.response.PlanResponse;
import com.glotrush.entities.Plan;

@Component
public class PlanBuilder {

    public PlanResponse mapToResponse(Plan plan) {
        if(plan == null) {
            return null;
        }

        return PlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .paymentInterval(plan.getPaymentInterval())
                .build();
    }
    
}
