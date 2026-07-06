package com.glotrush.services.stripe;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.glotrush.config.StripeConfig;
import com.glotrush.exceptions.StripeMessageException;
import com.glotrush.utils.LocaleUtils;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.Refund;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService implements IStripService {

    private final StripeConfig stripeConfig;
    private final MessageSource messageSource;

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
            throw new StripeMessageException(messageSource.getMessage("error.stripe.create.customer", null, LocaleUtils.getCurrentLocale()), e);
        }
    }

    @Override
    public String createCheckoutSession(String customerId, String priceId, String accountID, String planId) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setCustomer(customerId)
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(stripeConfig.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(stripeConfig.getCancelUrl())
                    .addLineItem(SessionCreateParams.LineItem.builder()
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
                throw new StripeMessageException(messageSource.getMessage("error.stripe.create.session", null, LocaleUtils.getCurrentLocale()), e);
            }
    }

    @Override
    public void cancelSubscription(String stripeSubscriptionId) {
        try {
            Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
            if ("canceled".equals(subscription.getStatus())) {
                return;
            }
            subscription.cancel();
        } catch (StripeException e) {
            throw new StripeMessageException(messageSource.getMessage("error.stripe.cancel", null, LocaleUtils.getCurrentLocale()), e);
        }
    }

    @Override
    public long cancelWithProrationRefund(String stripeSubscriptionId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        try {
            Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
            log.info("status={} latestInvoice={} periodStart={} periodEnd={}",
                subscription.getStatus(), subscription.getLatestInvoice(),
                subscription.getCurrentPeriodStart(), subscription.getCurrentPeriodEnd());

            if ("canceled".equals(subscription.getStatus())) {
                log.info("already canceled");
                return 0;
            }

            long refundAmount = 0;

            LocalDateTime effectivePeriodStart = periodStart != null ? periodStart : subscription.getCurrentPeriodStart() != null ? LocalDateTime.ofEpochSecond(subscription.getCurrentPeriodStart(), 0, ZoneOffset.UTC) : null;
            LocalDateTime effectivePeriodEnd = periodEnd != null ? periodEnd : subscription.getCurrentPeriodEnd() != null ? LocalDateTime.ofEpochSecond(subscription.getCurrentPeriodEnd(), 0, ZoneOffset.UTC) : null;

            log.info("effectivePeriodStart={} effectivePeriodEnd={}", effectivePeriodStart, effectivePeriodEnd);

            if (effectivePeriodStart != null && effectivePeriodEnd != null && subscription.getLatestInvoice() != null) {
                long totalDays = ChronoUnit.DAYS.between(effectivePeriodStart, effectivePeriodEnd);
                long remainingDays = ChronoUnit.DAYS.between(LocalDateTime.now(), effectivePeriodEnd);

                log.info("totalDays={} remainingDays={}", totalDays, remainingDays);

                if (totalDays > 0 && remainingDays > 0) {
                    Invoice invoice = Invoice.retrieve(subscription.getLatestInvoice());
                    refundAmount = (invoice.getAmountPaid() * remainingDays) / totalDays;

                    log.info("refundAmount={}", refundAmount);

                    if (refundAmount > 0 && invoice.getPaymentIntent() != null) {
                        Refund.create(RefundCreateParams.builder()
                            .setPaymentIntent(invoice.getPaymentIntent())
                            .setAmount(refundAmount)
                            .build()
                        );
                    } else {
                        log.info("skip: refundAmount={} paymentIntent={}", refundAmount, invoice.getPaymentIntent());
                    }
                } else {
                    log.info("skip: totalDays={} remainingDays={}", totalDays, remainingDays);
                }
            } else {
                log.info("skip: periodStart={} periodEnd={} latestInvoice={}",effectivePeriodStart, effectivePeriodEnd, subscription.getLatestInvoice());
            }

            subscription.cancel();
            return refundAmount;

        } catch (StripeException e) {
            throw new StripeMessageException(messageSource.getMessage("error.stripe.cancel", null, LocaleUtils.getCurrentLocale()), e);
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
            throw new StripeMessageException(messageSource.getMessage("error.stripe.schedule", null, LocaleUtils.getCurrentLocale()), e);
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
            throw new StripeMessageException(messageSource.getMessage("error.stripe.schedule.subscription", null, LocaleUtils.getCurrentLocale()), e);
        }
    
    }

    @Override
    public Session retrieveSession(String sessionId) {
        try {
            return Session.retrieve(sessionId);
        } catch (StripeException e) {
            throw new StripeMessageException(messageSource.getMessage("error.stripe.retrieve.session", null, LocaleUtils.getCurrentLocale()), e);
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
            throw new StripeMessageException(messageSource.getMessage("error.stripe.reactivate", null, LocaleUtils.getCurrentLocale()), e);
        }
    
    }
    
}
