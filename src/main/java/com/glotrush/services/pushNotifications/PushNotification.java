package com.glotrush.services.pushNotifications;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glotrush.entities.PushNotificationSubscription;
import com.glotrush.repositories.PushNotificationSubscriptionRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;

@Slf4j
@RequiredArgsConstructor
@Service
public class PushNotification implements IPushNotification {
    private final PushNotificationSubscriptionRepository subscriptionRepository;

    @Value("${vapid.public-key}")
    private String vapidPublicKey;
    
    @Value("${vapid.private-key}")
    private String vapidPrivateKey;

    @Value("${vapid.subject}")
    private String vapidSubject;

    @Override
    @Transactional
    public void sendNotification(UUID accountId, String title, String message) {
        log.info("=== sendNotification called for accountId={} title='{}' message='{}'", accountId, title, message);

        List<PushNotificationSubscription> subscriptions = subscriptionRepository.findByAccount_Id(accountId);
        log.info("Found {} subscription(s) for accountId={}", subscriptions.size(), accountId);

        if (subscriptions.isEmpty()) {
            return;
        }

        PushService pushService;
        String payload;
        try {
            pushService = buildPushService();
        } catch (Exception e) {
            log.error("Failed to build PushService: {}", e.getMessage());
            return;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            payload = objectMapper.writeValueAsString(Map.of("title", title, "body", message));
        } catch (Exception e) {
            log.error("Failed to serialize payload: {}", e.getMessage());
            return;
        }

        for (PushNotificationSubscription subscription : subscriptions) {
            try {
                Notification notification = new Notification(subscription.getEndpoint(), subscription.getPublicKey(), subscription.getAuth(), payload);

                HttpResponse response = pushService.send(notification, Encoding.AES128GCM);
                int statusCode = response.getStatusLine().getStatusCode();
                log.info(" response status={}", statusCode);

               
            } catch (Exception e) {
                log.error("  Exception message={}", e.getMessage());
                log.error("  Stacktrace:", e);
                if(e.getMessage() != null && (e.getMessage().contains("410") || e.getMessage().contains("404"))) {
                    subscriptionRepository.deleteByEndpoint(subscription.getEndpoint());
                }
            }
        }
    }

    private PushService buildPushService() throws Exception {
        return new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
    }
}
