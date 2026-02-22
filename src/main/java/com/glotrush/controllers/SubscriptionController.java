package com.glotrush.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.response.SubscriptionResponse;
import com.glotrush.services.subscription.ISubscriptionService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;



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

 



    
}
