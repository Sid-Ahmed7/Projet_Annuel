package com.glotrush.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelSubscriptionRequest {
    
    @Builder.Default
    private Boolean isCancelAtPeriodEnd = false;

    @Size(max = 500, message = "{error.cancellationReason.maxLength}")
    private String cancellationReason;
}
