package com.glotrush.dto.response.export;

import java.time.LocalDateTime;

import com.glotrush.enumerations.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
    
@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountExportResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private LocalDateTime createdAt;
    private int currentStreak;
    private int longestStreak;
    private UserRole role;
}