package com.glotrush.services.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationListener implements MessageListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            NotificationMessage notif = objectMapper.readValue(message.getBody(), NotificationMessage.class);
            notificationService.deliverToLocalEmitter(notif.getAccountId(), notif.getType(), notif.getMessage());
        } catch (Exception e) {
            log.error("Failed to process Redis notification", e);
        }
    }
}
