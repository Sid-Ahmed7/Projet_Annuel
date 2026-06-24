package com.glotrush.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.request.answer.UserAnswerResultRequest;
import com.glotrush.dto.response.answers.UserDailyQuestion;
import com.glotrush.dto.response.answers.UserMistakeListResponse;
import com.glotrush.dto.response.answers.UserMistakeRetryListResponse;
import com.glotrush.dto.response.answers.UserResultResponse;
import com.glotrush.services.rewiewMistake.IReviewMistakeService;
import com.glotrush.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/review-mistakes")
@RequiredArgsConstructor
public class ReviewMistakeController {

    private final IReviewMistakeService reviewMistakeService;

    @GetMapping("/daily-question")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDailyQuestion> getDailyMistakeQuestion(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(reviewMistakeService.getDailyMistakeQuestion(accountId));
    }

    @GetMapping("/pending-mistakes")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserMistakeListResponse> getPendingMistakes(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(reviewMistakeService.getPendingMistakesQuestion(accountId));
    }

    @GetMapping("/pending-mistakes/{mistakeId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserMistakeRetryListResponse> getMistakeDetail(Authentication authentication, @PathVariable UUID mistakeId) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(reviewMistakeService.getMistakeQuestionDetail(accountId, mistakeId));
    }

    @PostMapping("/session/complete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResultResponse> submitAnswers(Authentication authentication, @RequestBody UserAnswerResultRequest request) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(reviewMistakeService.submitAnswer(accountId, request));
    }
}
