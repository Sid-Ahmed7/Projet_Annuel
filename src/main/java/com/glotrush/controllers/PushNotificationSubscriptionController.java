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
import org.springframework.web.bind.annotation.RequestParam;
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
        subscriptionRepository.findByEndpoint(request.getEndpoint()).ifPresentOrElse(
            existing -> {
                if (!existing.getAccount().getId().equals(accountId)) {
                    existing.setAccount(account);
                    subscriptionRepository.save(existing);
                }
            },
            () -> subscriptionRepository.save(
                PushNotificationSubscription.builder()
                    .account(account)
                    .endpoint(request.getEndpoint())
                    .publicKey(request.getPublicKey())
                    .auth(request.getAuth())
                    .build()
            )
        );

        return ResponseEntity.ok().build();    
    }



    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> getStatus(@RequestParam String endpoint, Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        boolean subscribed = subscriptionRepository.findByEndpoint(endpoint)
            .map(sub -> sub.getAccount().getId().equals(accountId))
            .orElse(false);
        return ResponseEntity.ok(subscribed);
    }

    @DeleteMapping("/unsubscribe")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unsubscribe(@RequestBody PushSubscriptionRequest request, Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        subscriptionRepository.deleteByEndpointAndAccount_Id(request.getEndpoint(), accountId);
        return ResponseEntity.ok().build();
    }
}
