package com.glotrush.builder;

import java.time.LocalDateTime;

import com.glotrush.entities.Accounts;
import com.glotrush.entities.TwoFactorAuth;

public class TwoFactorAuthBuilder {
        public TwoFactorAuth buildTwoFactorAuth(Accounts account, String encryptedSecret) {
            return TwoFactorAuth.builder()
                .account(account)
                .secret(encryptedSecret)
                .active(false)
                .createdAt(LocalDateTime.now())
                .build();
        }

}
