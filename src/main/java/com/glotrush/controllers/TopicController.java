package com.glotrush.controllers;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.request.ExamResultRequest;
import com.glotrush.dto.request.TopicRequest;
import com.glotrush.dto.response.ApiResponse;
import com.glotrush.dto.response.CompleteExamResponse;
import com.glotrush.dto.response.CompleteLessonResponse;
import com.glotrush.dto.response.ExamResponse;
import com.glotrush.enumerations.ProficiencyLevel;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.glotrush.dto.response.TopicResponse;
import com.glotrush.services.topic.ITopicService;
import com.glotrush.utils.LocaleUtils;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
public class TopicController {

    private final ITopicService topicService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopicResponse>> getAllTopics() {
        List<TopicResponse> topics = topicService.getAllTopics();
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<TopicResponse>> getAllTopicsByActive(Authentication authentication) {
        UUID accountId = authentication != null ? UUID.fromString(authentication.getName()) : null;
        List<TopicResponse> topics = topicService.getAllTopics(accountId);
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopicResponse>> searchTopics(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ProficiencyLevel difficulty,
            @RequestParam(required = false) Boolean isActive) {
        List<TopicResponse> topics = topicService.searchTopics(name, difficulty, isActive);
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/search/active")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<TopicResponse>> searchActiveTopics(
            @RequestParam UUID languageId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ProficiencyLevel difficulty) {
        List<TopicResponse> topics = topicService.searchActiveTopics(languageId, name, difficulty);
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/language/{languageId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<TopicResponse>> getTopicsByLanguage(Authentication authentication, @PathVariable UUID languageId) {
        UUID accountId = authentication != null ? UUID.fromString(authentication.getName()) : null;
        List<TopicResponse> topics = topicService.getTopicsByLanguage(languageId, accountId);
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/{topicId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TopicResponse> getTopicById(Authentication authentication, @PathVariable UUID topicId) {
        UUID accountId = authentication != null ? UUID.fromString(authentication.getName()) : null;
        TopicResponse topic = topicService.getTopicById(topicId, accountId);
        return ResponseEntity.ok(topic);
    }

    @GetMapping("/{topicId}/exam")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ExamResponse> getTopicExam(Authentication authentication, @PathVariable UUID topicId) {
        UUID accountId = UUID.fromString(authentication.getName());
        ExamResponse exam = topicService.generateTopicExam(accountId, topicId);
        return ResponseEntity.ok(exam);
    }

    @PostMapping("/{topicId}/exam/submit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CompleteExamResponse> submitTopicExam(Authentication authentication, @PathVariable UUID topicId, @Valid @RequestBody ExamResultRequest examRequest) {
        UUID accountId = UUID.fromString(authentication.getName());
        CompleteExamResponse response = topicService.completeTopicExam(accountId, topicId, examRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TopicResponse> createTopic(@Valid @RequestBody TopicRequest topicRequest){
        TopicResponse topicResponse = topicService.createTopic(topicRequest);
        return ResponseEntity.ok(topicResponse);
    }

    @PutMapping("/{topicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TopicResponse> updateTopic(@PathVariable UUID topicId, @Valid @RequestBody TopicRequest topicRequest){
        TopicResponse topicResponse = topicService.updateTopic(topicId, topicRequest);
        return ResponseEntity.ok(topicResponse);
    }

    @DeleteMapping("/{topicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteTopic(@PathVariable UUID topicId){
        topicService.removeTopic(topicId);
        return ResponseEntity.ok(new ApiResponse(messageSource.getMessage("info.topic.deleted_successfully", null, LocaleUtils.getCurrentLocale())));
    }
}
