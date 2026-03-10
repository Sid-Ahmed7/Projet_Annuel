package com.glotrush.services.subscription;

import java.util.UUID;

import com.glotrush.dto.response.SubscriptionResponse;
import com.glotrush.entities.Accounts;

public interface ISubscriptionService {

    void createSubscriptionForUser(Accounts account);
    SubscriptionResponse getSubscription(UUID accountId);
    void checkAndChangeExpiredSubscriptions();
    void sendEmailWhenExpiringSoon();

    void expireSingleSubscription(UUID subscriptionId);
    void sendReminderEmailForExpiringSubscription(UUID subscriptionId);

}
