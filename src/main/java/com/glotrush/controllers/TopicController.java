package com.glotrush.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.response.TopicResponse;
import com.glotrush.services.topic.ITopicService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
public class TopicController {

    private final ITopicService topicService;

    @GetMapping
    public ResponseEntity<List<TopicResponse>> getAllTopics(Authentication authentication) {
        UUID accountId = authentication != null ? UUID.fromString(authentication.getName()) : null;
        List<TopicResponse> topics = topicService.getAllTopics(accountId);
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/language/{languageId}")
    public ResponseEntity<List<TopicResponse>> getTopicsByLanguage(Authentication authentication, @PathVariable UUID languageId) {
        UUID accountId = authentication != null ? UUID.fromString(authentication.getName()) : null;
        List<TopicResponse> topics = topicService.getTopicsByLanguage(languageId, accountId);
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/{topicId}")
    public ResponseEntity<TopicResponse> getTopicById(Authentication authentication, @PathVariable UUID topicId) {
        UUID accountId = authentication != null ? UUID.fromString(authentication.getName()) : null;
        TopicResponse topic = topicService.getTopicById(topicId, accountId);
        return ResponseEntity.ok(topic);
    }
}
