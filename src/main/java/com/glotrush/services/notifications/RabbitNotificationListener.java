package com.glotrush.services.notifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.glotrush.dto.request.NotificationMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitNotificationListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "#{sseQueue.name}")
    public void onMessage(NotificationMessage message) {
        notificationService.deliverToLocalEmitter(message.getAccountId(), message.getType(), message.getMessage());
    }
}