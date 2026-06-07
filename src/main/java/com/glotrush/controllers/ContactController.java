package com.glotrush.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;

import com.glotrush.dto.request.ContactRequest;
import com.glotrush.services.EmailService;

@RestController
@RequestMapping("api/v1/contact")
@RequiredArgsConstructor
public class ContactController {

    private final EmailService emailService;

    @PostMapping
    @RateLimiter(name = "contact")
    public ResponseEntity<Void> sendContactEmail(@RequestBody ContactRequest contactRequest) {
        emailService.sendContactEmail(contactRequest);
        return ResponseEntity.ok().build();
    }
    
}
