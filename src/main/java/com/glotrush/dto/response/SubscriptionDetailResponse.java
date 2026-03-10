package com.glotrush.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.glotrush.enumerations.SubscriptionStatus;

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
    private UUID accountId;
    private String firstName;
    private String lastName;
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
