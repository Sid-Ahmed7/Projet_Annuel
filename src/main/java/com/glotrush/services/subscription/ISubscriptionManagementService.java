package com.glotrush.services.subscription;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.request.CancelSubscriptionRequest;
import com.glotrush.dto.request.ChangePlanRequest;
import com.glotrush.dto.request.SubscribeToPlanRequest;
import com.glotrush.dto.response.CheckoutStripeResponse;
import com.glotrush.dto.response.PaymentHistoryResponse;
import com.glotrush.dto.response.SubscriptionDetailResponse;
import com.glotrush.dto.response.SubscriptionStatsResponse;

public interface ISubscriptionManagementService {

    List<SubscriptionDetailResponse> getAllSubscriptions();
    SubscriptionDetailResponse getSubscriptionDetail(UUID accountId);
    CheckoutStripeResponse subscribeToPlan(UUID accountId, SubscribeToPlanRequest request);
    CheckoutStripeResponse changeSubscriptionPlan(UUID accountId, ChangePlanRequest request);
    SubscriptionDetailResponse cancelSubscription(UUID accountId, CancelSubscriptionRequest request);
    SubscriptionDetailResponse reactivateSubscription(UUID accountId);
    List<PaymentHistoryResponse> getPaymentHistory(UUID accountId);
    SubscriptionStatsResponse getSubscriptionStats();

}

