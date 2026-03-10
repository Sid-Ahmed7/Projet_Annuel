package com.glotrush.builder;

import java.util.List;

import org.springframework.stereotype.Component;

import com.glotrush.dto.response.PaymentHistoryResponse;
import com.glotrush.entities.PaymentHistory;

@Component
public class PaymentHistoryBuilder {
    
    public PaymentHistoryResponse mapToResponse(PaymentHistory paymentHistory) {
        if(paymentHistory == null) {
            return null;
        }

        return PaymentHistoryResponse.builder()
                .id(paymentHistory.getId())
                .transactionId(paymentHistory.getTransactionId())
                .amount(paymentHistory.getAmount())
                .currency(paymentHistory.getCurrency())
                .status(paymentHistory.getPaymentStatus())
                .paymentMethod(paymentHistory.getPaymentMethod())
                .paidAt(paymentHistory.getPaymentAt())
                .createdAt(paymentHistory.getCreatedAt())
                .build();
    }

    public List<PaymentHistoryResponse> mapToResponseList(List<PaymentHistory> paymentHistories) {

        return paymentHistories.stream()
                .map(this::mapToResponse)
                .toList();
    }
}
