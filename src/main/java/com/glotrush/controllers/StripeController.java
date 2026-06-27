package com.glotrush.controllers;

import java.io.IOException;
import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.config.StripeConfig;
import com.glotrush.services.stripe.IStripService;
import com.glotrush.services.stripe.StripeWebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeController {

    private final StripeConfig stripeConfig;
    private final StripeWebhookService stripeWebhookService;
    private final IStripService stripService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(HttpServletRequest request, @RequestHeader("Stripe-Signature") String stripeHeader) {

        String data;
        try {
            data = new String(request.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read webhook request body", e);
            return ResponseEntity.badRequest().body("Failed to read request body");
        }

        Event event;
        try {
            event = Webhook.constructEvent(data, stripeHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.error("Signature verification failed", e);
            return ResponseEntity.badRequest().body("Invalid signature");
        }
          switch (event.getType()) {
            case "checkout.session.completed" -> {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if (session != null) {
                    stripeWebhookService.finalizeCheckout(session);
                }
            }
            case "customer.subscription.deleted" -> {
                Subscription subscription = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
                if (subscription != null) {
                    stripeWebhookService.handleSubscriptionDeleted(subscription);
                }
            }
            case "invoice.paid" -> {
                Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
                if (invoice != null && invoice.getSubscription() != null) {
                    stripeWebhookService.renewSubscription(invoice);
                }
            }
            case "invoice.payment_failed" -> {
                Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
                if (invoice != null) {
                    stripeWebhookService.handlePaymentFailed(invoice);
                }
            }

            default -> log.info("event ignored: {}", event.getType());
        }

        return ResponseEntity.ok("Webhook received");
    }

    @GetMapping("/verify/{sessionId}")
    public ResponseEntity<Void> verifyCheckoutSession(@PathVariable String sessionId, Principal principal) {
        Session session = stripService.retrieveSession(sessionId);
        if ("paid".equals(session.getPaymentStatus())) {
            stripeWebhookService.finalizeCheckout(session);
        }
        return ResponseEntity.ok().build();
    }

}
