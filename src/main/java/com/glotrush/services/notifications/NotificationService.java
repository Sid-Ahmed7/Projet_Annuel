package com.glotrush.services.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public SseEmitter subscribe(UUID accountId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(accountId, emitter);
        emitter.onCompletion(() -> emitters.remove(accountId));
        emitter.onTimeout(() -> emitters.remove(accountId));
        emitter.onError((e) -> emitters.remove(accountId));
        return emitter;
    }

    public void sendNotification(UUID accountId, String type, String message) {
        try {
            String json = objectMapper.writeValueAsString(new NotificationMessage(accountId, type, message));
            redisTemplate.convertAndSend("notifications", json);
        } catch (Exception e) {
            log.error("Failed to publish to Redis ", e);
        }
    }

    public void deliverToLocalEmitter(UUID accountId, String type, String message) {
        SseEmitter emitter = emitters.get(accountId);
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event().name(type).data(Map.of("message", message, "type", type)));
        } catch (IOException e) {
            emitters.remove(accountId);
        }
    }
}