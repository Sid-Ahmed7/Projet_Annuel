package com.glotrush.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.request.CancelSubscriptionRequest;
import com.glotrush.dto.request.ChangePlanRequest;
import com.glotrush.dto.request.SubscribeToPlanRequest;
import com.glotrush.dto.response.CheckoutStripeResponse;
import com.glotrush.dto.response.PaymentHistoryResponse;
import com.glotrush.dto.response.SubscriptionDetailResponse;
import com.glotrush.dto.response.SubscriptionResponse;
import com.glotrush.dto.response.SubscriptionStatsResponse;
import com.glotrush.services.subscription.ISubscriptionService;
import com.glotrush.services.subscription.SubscriptionManagementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final ISubscriptionService subscriptionService;
    private final SubscriptionManagementService subscriptionManagementService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all-subscriptions")
    public ResponseEntity<List<SubscriptionDetailResponse>> getSubscriptions() {
        return ResponseEntity.ok(subscriptionManagementService.getAllSubscriptions());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{accountId}")
    public ResponseEntity<SubscriptionDetailResponse> getSubscriptionOfAccount(@PathVariable UUID accountId){
        return ResponseEntity.ok(subscriptionManagementService.getSubscriptionDetail(accountId));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{accountId}/cancel")
    public ResponseEntity<SubscriptionDetailResponse> cancelSubscriptionByAdmin(@PathVariable UUID accountId,@Valid @RequestBody CancelSubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionManagementService.cancelSubscription(accountId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{accountId}/payments")
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentHistoryByAccountId(@PathVariable UUID accountId) {
        return ResponseEntity.ok(subscriptionManagementService.getPaymentHistory(accountId));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/my-subscription")
    public ResponseEntity<SubscriptionResponse> getMySubscription(Authentication authentication) {
        UUID accountId = UUID.fromString(authentication.getName());
        SubscriptionResponse subscription = subscriptionService.getSubscription(accountId);
        return ResponseEntity.ok(subscription);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/detail")
    public ResponseEntity<SubscriptionDetailResponse> getMySubscriptionDetail(Authentication authentication) {
        UUID accountId = UUID.fromString(authentication.getName());
        SubscriptionDetailResponse subscriptionDetail = subscriptionManagementService.getSubscriptionDetail(accountId);
        return ResponseEntity.ok(subscriptionDetail);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/subscribe")
    public ResponseEntity<CheckoutStripeResponse> subscribeToPlan(Authentication authentication, @Valid @RequestBody SubscribeToPlanRequest request) {
        UUID accountId = UUID.fromString(authentication.getName());
        CheckoutStripeResponse response = subscriptionManagementService.subscribeToPlan(accountId, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/change-plan")
    public ResponseEntity<CheckoutStripeResponse> changeSubscriptionPlan(Authentication authentication, @Valid @RequestBody ChangePlanRequest request) {
        UUID accountId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(subscriptionManagementService.changeSubscriptionPlan(accountId, request));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/cancel")
    public ResponseEntity<SubscriptionDetailResponse> cancelSubscription(Authentication authentication, @Valid @RequestBody CancelSubscriptionRequest request) {
        UUID accountId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(subscriptionManagementService.cancelSubscription(accountId, request));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/reactivate")
    public ResponseEntity<SubscriptionDetailResponse> reactivateSubscription(Authentication authentication) {
        UUID accountId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(subscriptionManagementService.reactivateSubscription(accountId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentHistory(Authentication authentication) {
        UUID accountId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(subscriptionManagementService.getPaymentHistory(accountId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<SubscriptionStatsResponse> getSubscriptionStats() {
        return ResponseEntity.ok(subscriptionManagementService.getSubscriptionStats());
    }

}
