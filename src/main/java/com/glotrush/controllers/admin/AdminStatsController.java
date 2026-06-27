package com.glotrush.controllers.admin;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.response.stats.LearningStatsResponse;
import com.glotrush.dto.response.stats.UserDetailsResponse;
import com.glotrush.dto.response.stats.UserEngagementResponse;
import com.glotrush.dto.response.stats.UserStatsListResponse;
import com.glotrush.services.admin.IAdminStatsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final IAdminStatsService adminStatsService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/engagement")
    public ResponseEntity<UserEngagementResponse> getUserEngagement() {
        return ResponseEntity.ok(adminStatsService.getUserEngagement());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/learning")
    public ResponseEntity<LearningStatsResponse> getLearningMetrics() {
        return ResponseEntity.ok(adminStatsService.getLearningMetrics());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<UserStatsListResponse> getUserStats(@RequestParam(required = false) String search, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(adminStatsService.getUserStats(search, page, size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDetailsResponse> getUserDetail(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminStatsService.getUserDetail(userId));
    }
}