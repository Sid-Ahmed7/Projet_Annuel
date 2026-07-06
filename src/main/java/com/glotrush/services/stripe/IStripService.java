package com.glotrush.services.stripe;

import java.time.LocalDateTime;

import com.stripe.model.checkout.Session;

public interface IStripService {

    String createCustomer(String email, String name);
    String createCheckoutSession(String customerId, String priceId, String accountID, String planId);
    void cancelSubscription(String stripeSubscriptionId);
    long cancelWithProrationRefund(String stripeSubscriptionId, LocalDateTime periodStart, LocalDateTime periodEnd);
    void cancelSubscriptionAtPeriodEnd(String stripeSubscriptionId);
    void reactivateSubscription(String stripeSubscriptionId);
    void schedulePlanChange(String stripeSubscriptionId, String newPriceId);
    Session retrieveSession(String sessionId);

}
