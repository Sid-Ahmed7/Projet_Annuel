package com.glotrush.services.notifications;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Service
public class NotificationService {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    

    public SseEmitter subscribe(UUID accountId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(accountId, emitter);
        emitter.onCompletion(() -> emitters.remove(accountId));
        emitter.onTimeout(() -> emitters.remove(accountId));
        emitter.onError((e) -> emitters.remove(accountId));
        return emitter;
    }

    public void sendNotification(UUID accountID, String type, String message) {
        SseEmitter emitter = emitters.get(accountID);
        if(emitter == null) {
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name(type)
                    .data(Map.of("message", message, "type", type)));
        } catch(IOException e) {
            emitters.remove(accountID);
        }
    }

}
