package com.glotrush.dto.response.export;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.glotrush.enumerations.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryExportResponse {
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private LocalDateTime paymentAt;
}
