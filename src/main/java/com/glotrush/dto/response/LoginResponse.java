package com.glotrush.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private UUID userId;
    private String email;
    private String username;
    private String role;
    private String accessToken;
    private String refreshToken;
    private Boolean requires2FA;
    private String tempUserId;
    private String message;
}
