package com.glotrush.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailsResponse {
    private UUID accountId;
    private String username;
    private String email;
    private String status;
    private String role;
    private Integer currentStreak;
    private LocalDateTime createdAt;
    private Boolean bannedOfReview;
    private Integer failedLoginAttempts;
}
