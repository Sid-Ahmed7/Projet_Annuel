package com.glotrush.builder;

import org.springframework.stereotype.Component;

import com.glotrush.dto.response.AdminUserDetailsResponse;
import com.glotrush.entities.Accounts;

@Component
public class AdminUserBuilder {

    public AdminUserDetailsResponse buildUserDetailsResponse(Accounts account) {
        return AdminUserDetailsResponse.builder()
                .accountId(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .status(account.getStatus().name())
                .role(account.getRole().name())
                .currentStreak(account.getCurrentStreak())
                .createdAt(account.getCreatedAt())
                .bannedOfReview(account.getIsBannedOfReview())
                .failedLoginAttempts(account.getFailedLoginAttempts())
                .build();
    }
}