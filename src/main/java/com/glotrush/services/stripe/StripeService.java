package com.glotrush.services.stripe;

import org.springframework.stereotype.Service;

import com.glotrush.config.StripeConfig;
import com.glotrush.exceptions.StripeMessageException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class StripeService implements IStripService {

    private final StripeConfig stripeConfig;

    @Override
    public String createCustomer(String email, String name) {
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(email)
                    .setName(name)
                    .build();
            Customer customer = Customer.create(params);
            return customer.getId();
        } catch(StripeException e) {
            throw new StripeMessageException("Failed to create Stripe customer", e);
        }
    }

    @Override
    public String createCheckoutSession(String customerId, String priceId, String accountID, String planId) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setCustomer(customerId)
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(stripeConfig.getSuccessUrl())
                    .setCancelUrl(stripeConfig.getCancelUrl())
                    .addLineItem(
                        SessionCreateParams.LineItem.builder()
                            .setPrice(priceId)
                            .setQuantity(1L)
                            .build()
                ) 
                .putMetadata("account_id", accountID)
                .putMetadata("plan_id", planId)
                .build();
                Session session = Session.create(params);
                return session.getUrl();

            } catch(StripeException e) {
                throw new StripeMessageException("Failed to create Stripe checkout session", e);
            }
    }

    @Override
    public void cancelSubscription(String stripeSubscriptionId) {

        try {
            Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
            subscription.cancel();
        } catch(StripeException e) {
            throw new StripeMessageException("Failed to cancel Stripe subscription", e);
        }
        
    }

    @Override
    public void schedulePlanChange(String stripeSubscriptionId, String newPriceId) {

        try {
            Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
            String currentItemId = subscription.getItems().getData().get(0).getId();
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
            .addItem(SubscriptionUpdateParams.Item.builder()
                    .setId(currentItemId)
                    .setPrice(newPriceId)
                    .build())
            .setProrationBehavior(SubscriptionUpdateParams.ProrationBehavior.NONE)
            .setBillingCycleAnchor(SubscriptionUpdateParams.BillingCycleAnchor.UNCHANGED)
            .build();
            subscription.update(params);
        } catch(StripeException e) {
            throw new StripeMessageException("Failed to schedule plan change", e);
        }
    }

    @Override
    public void cancelSubscriptionAtPeriodEnd(String stripeSubscriptionId) {
        try {
            Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build();
            subscription.update(params);
        } catch(StripeException e) {
            throw new StripeMessageException("Failed to schedule subscription cancellation at period end", e);
        }
    
    }

    @Override
    public void reactivateSubscription(String stripeSubscriptionId) {
        try {
            Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(false)
                    .build();
            subscription.update(params);
        } catch(StripeException e) {
            throw new StripeMessageException("Failed to reactivate subscription", e);
        }
    
    }
    
}
