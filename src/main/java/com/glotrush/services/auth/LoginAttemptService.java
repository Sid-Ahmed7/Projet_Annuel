package com.glotrush.services.auth;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.glotrush.constants.SecurityConstants;
import com.glotrush.entities.Accounts;
import com.glotrush.repositories.AccountsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final AccountsRepository accountsRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean handleFailedLogin(Accounts account) {
        Accounts freshAccount = accountsRepository.findById(account.getId()).orElse(account);
        int attempts = freshAccount.getFailedLoginAttempts() + 1;
        freshAccount.setFailedLoginAttempts(attempts);

        boolean locked = attempts >= SecurityConstants.MAX_LOGIN_ATTEMPTS;
        if (locked) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(SecurityConstants.ACCOUNT_LOCK_DURATION_MINUTES);
            freshAccount.setAccountLockedUntil(lockUntil);
        }

        accountsRepository.save(freshAccount);
        return locked;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedLoginAttempts(Accounts account) {
        if (account.getFailedLoginAttempts() > 0) {
            Accounts freshAccount = accountsRepository.findById(account.getId()).orElse(account);
            freshAccount.setFailedLoginAttempts(0);
            freshAccount.setAccountLockedUntil(null);
            accountsRepository.save(freshAccount);
        }
    }
}
