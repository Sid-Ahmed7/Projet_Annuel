package com.glotrush.dto.request;

import java.math.BigDecimal;

import com.glotrush.enumerations.PaymentInterval;
import com.glotrush.enumerations.SubscriptionType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlanRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    private BigDecimal price;

    @Size(max = 3)
    private String currency;

    private PaymentInterval paymentInterval;

    @NotNull
    private SubscriptionType subscriptionType;

    private String stripePriceId;
}
