package com.glotrush.controllers;

import java.util.UUID;

import javax.security.auth.login.AccountNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.request.PushSubscriptionRequest;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.PushNotificationSubscription;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.PushNotificationSubscriptionRepository;
import com.glotrush.utils.LocaleUtils;
import com.glotrush.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/push-notifications")
public class PushNotificationSubscriptionController {

    private final PushNotificationSubscriptionRepository subscriptionRepository;
    private final AccountsRepository accountsRepository;
    private final MessageSource messageSource;

    @Value("${vapid.public-key}")
    private String vapidPublicKey;

    @GetMapping("/vapid-public-key")
    public ResponseEntity<String> getVapidPublicKey() {
        return ResponseEntity.ok(vapidPublicKey);
    }

    @PostMapping("/subscribe")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> subscrible(@RequestBody PushSubscriptionRequest request, Authentication authentication) throws AccountNotFoundException {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        Accounts account = accountsRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(messageSource.getMessage("account.notfound", null, LocaleUtils.getCurrentLocale()) ));
        if(!subscriptionRepository.existsByEndpoint(request.getEndpoint())) {
            PushNotificationSubscription subscription = PushNotificationSubscription.builder()
                .account(account)
                .endpoint(request.getEndpoint())
                .publicKey(request.getPublicKey())
                .auth(request.getAuth())
                .build();
            subscriptionRepository.save(subscription);
        }

        return ResponseEntity.ok().build();    
    }



    @DeleteMapping("/unsubscribe")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unsubscribe(@RequestBody PushSubscriptionRequest request) {
        subscriptionRepository.deleteByEndpoint(request.getEndpoint());
        return ResponseEntity.ok().build();
    }
}
