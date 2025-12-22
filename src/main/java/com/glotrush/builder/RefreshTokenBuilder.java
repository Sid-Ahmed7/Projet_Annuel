package com.glotrush.builder;

import java.time.Duration;
import java.time.LocalDateTime;

import com.glotrush.entities.Accounts;
import com.glotrush.entities.RefreshToken;

public class RefreshTokenBuilder {
        public RefreshToken buildRefreshToken(Accounts account, String token, String deviceInfo, long expirationMillis) {
            return RefreshToken.builder()
                .account(account)
                .token(token)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(expirationMillis)))
                .deviceInfo(deviceInfo)
                .isRevoked(false)
                .build();
                    
        }

}
