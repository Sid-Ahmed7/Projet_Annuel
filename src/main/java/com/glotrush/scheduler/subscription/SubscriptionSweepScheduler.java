package com.glotrush.scheduler.subscription;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.glotrush.services.subscription.ISubscriptionService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionSweepScheduler {

    private final ISubscriptionService subscriptionService;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void sweepExpiredSubscriptions() {
        log.info("Running daily subscription expiration sweep");
        subscriptionService.checkAndChangeExpiredSubscriptions();
        log.info("Daily subscription expiration sweep completed");
    }

    @Scheduled(cron = "0 0 12 * * *")
    @Transactional
    public void sweepExpiringSoonReminders() {
        log.info("Running daily subscription expiry reminder sweep");
        subscriptionService.sendEmailWhenExpiringSoon();
        log.info("Daily subscription expiry reminder sweep completed");
    }
}