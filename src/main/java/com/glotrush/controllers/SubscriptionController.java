package com.glotrush.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.request.ChangeSubscriptionRequest;
import com.glotrush.dto.response.SubscriptionResponse;
import com.glotrush.services.subscription.ISubscriptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final ISubscriptionService subscriptionService;

    @GetMapping("/my-subscription")
    public ResponseEntity<SubscriptionResponse> getMySubscription(Authentication authentication) {
        UUID accountId = UUID.fromString(authentication.getName());
        SubscriptionResponse subscription = subscriptionService.getSubscription(accountId);
        return ResponseEntity.ok(subscription);
    }

    @PutMapping("/change-subscription")
    public ResponseEntity<SubscriptionResponse> changeUserSubscription(Authentication authentication, @Valid @RequestBody ChangeSubscriptionRequest request) {
        UUID accountId = UUID.fromString(authentication.getName());
        SubscriptionResponse subscription = subscriptionService.changeSubscriptionType(accountId, request);
        return ResponseEntity.ok(subscription);

    }



    
}
