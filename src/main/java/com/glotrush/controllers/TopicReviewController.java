package com.glotrush.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.request.TopicReviewRequest;
import com.glotrush.dto.response.ApiResponse;
import com.glotrush.dto.response.TopicReviewResponse;
import com.glotrush.dto.response.TopicReviewsResponse;
import com.glotrush.services.review.ITopicReviewService;
import com.glotrush.utils.LocaleUtils;
import com.glotrush.utils.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class TopicReviewController {
    
    private final ITopicReviewService topicReviewService;
    private final MessageSource messageSource;


    @PostMapping("/topic/{topicId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TopicReviewResponse> addReview(Authentication authentication, @PathVariable UUID topicId, @Valid @RequestBody TopicReviewRequest request) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        TopicReviewResponse reviewResponse = topicReviewService.addReview(accountId, topicId, request);
        return ResponseEntity.ok(reviewResponse);
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TopicReviewResponse> updateReview(Authentication authentication, @PathVariable UUID reviewId, @Valid @RequestBody TopicReviewRequest request) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        TopicReviewResponse reviewResponse = topicReviewService.updateReview(accountId, reviewId, request);
        return ResponseEntity.ok(reviewResponse);
    }

    @GetMapping("/topic/{topicId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TopicReviewsResponse> getTopicReviews(@PathVariable UUID topicId) {
        return ResponseEntity.ok(topicReviewService.getTopicReviews(topicId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopicReviewResponse>> getAllReviews() {
        return ResponseEntity.ok(topicReviewService.getAllReview());
    }

    @GetMapping("/topic/{topicId}/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TopicReviewResponse> getUserReview(Authentication authentication, @PathVariable UUID topicId) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        TopicReviewResponse review = topicReviewService.getUserReview(accountId, topicId);
        return ResponseEntity.ok(review);
    }


    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopicReviewResponse>> getPendingReviews() {
        return ResponseEntity.ok(topicReviewService.getPendingReview());
    }

    @PutMapping("/{reviewId}/accept")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TopicReviewResponse> acceptReview(@PathVariable UUID reviewId) {
        return ResponseEntity.ok(topicReviewService.acceptReview(reviewId));
    }

    @PutMapping("/{reviewId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectReview(@PathVariable UUID reviewId) {
        topicReviewService.rejectReview(reviewId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> deleteReview(@PathVariable UUID reviewId) {
        topicReviewService.deleteReview(reviewId);
        return ResponseEntity.ok(new ApiResponse(messageSource.getMessage("info_review_delete_success", null, LocaleUtils.getCurrentLocale())));
    }
}
