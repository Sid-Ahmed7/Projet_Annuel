package com.glotrush.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.response.UserStatisticsResponse;
import com.glotrush.services.admin.IAdminStatisticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final IAdminStatisticsService adminStatisticsService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserStatisticsResponse>> getAllUsersStatistics() {
        List<UserStatisticsResponse> statistics = adminStatisticsService.getAllUsersStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserStatisticsResponse> getUserStatisticsById(@PathVariable UUID userId) {
        UserStatisticsResponse statistics = adminStatisticsService.getUserStatisticsById(userId);
        return ResponseEntity.ok(statistics);
    }
}
