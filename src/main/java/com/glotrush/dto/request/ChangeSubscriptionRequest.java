package com.glotrush.dto.request;

import com.glotrush.enumerations.SubscriptionType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeSubscriptionRequest {
    
    @NotNull(message = "Subscription type is required")
    private SubscriptionType subscriptionType;
}
