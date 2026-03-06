package com.glotrush.services.stripe;

public interface IStripService {

    String createCustomer(String email, String name);
    String createCheckoutSession(String customerId, String priceId, String accountID, String planId);
    void cancelSubscription(String stripeSubscriptionId);
    void cancelSubscriptionAtPeriodEnd(String stripeSubscriptionId);
    void reactivateSubscription(String stripeSubscriptionId);
    void schedulePlanChange(String stripeSubscriptionId, String newPriceId);
    
}
