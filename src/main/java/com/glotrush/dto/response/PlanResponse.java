package com.glotrush.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.glotrush.enumerations.PaymentInterval;
import com.glotrush.enumerations.SubscriptionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {
    
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private PaymentInterval paymentInterval;
    private SubscriptionType subscriptionType;
    private Boolean isActive;

}
