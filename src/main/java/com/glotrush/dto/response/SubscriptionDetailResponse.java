package com.glotrush.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.glotrush.enumerations.SubscriptionStatus;
import com.glotrush.enumerations.SubscriptionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDetailResponse {

    private UUID id;
    private SubscriptionType subscriptionType;
    private SubscriptionStatus status;
    private PlanResponse plan;
    private Boolean isActive;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private Boolean cancelAtPeriodEnd;
    private LocalDateTime canceledAt;
    private Long daysRemaining;
    
}
