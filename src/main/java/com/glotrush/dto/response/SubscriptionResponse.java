package com.glotrush.dto.response;

import java.util.UUID;

import com.glotrush.enumerations.SubscriptionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private UUID id;
    private SubscriptionType subscriptionType;
    private Boolean isActive;
    private String startDate;
    private String endDate;
}
