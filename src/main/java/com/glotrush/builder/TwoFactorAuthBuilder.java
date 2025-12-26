package com.glotrush.builder;

import org.springframework.stereotype.Component;

import com.glotrush.entities.Accounts;
import com.glotrush.entities.TwoFactorAuth;

@Component
public class TwoFactorAuthBuilder {
        public TwoFactorAuth buildTwoFactorAuth(Accounts account, String encryptedSecret) {
            return TwoFactorAuth.builder()
                .account(account)
                .secret(encryptedSecret)
                .active(false)
                .build();
        }

}
