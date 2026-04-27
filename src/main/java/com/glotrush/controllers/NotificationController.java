package com.glotrush.controllers;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.glotrush.services.notifications.NotificationService;
import com.glotrush.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;

    @GetMapping(value = "/stream", produces =  MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public SseEmitter streamNotifications(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        return notificationService.subscribe(accountId);
    }
}
