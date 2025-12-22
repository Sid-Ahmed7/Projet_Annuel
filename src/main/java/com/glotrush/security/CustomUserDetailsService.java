package com.glotrush.security;

import java.time.LocalDateTime;
import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.glotrush.entities.Accounts;
import com.glotrush.enumerations.AccountStatus;
import com.glotrush.exceptions.AccountLockedException;
import com.glotrush.repositories.AccountsRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
@Service 
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountsRepository accountsRepository;

    @Override
    @Transactional()
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Accounts account = accountsRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (account.getAccountLockedUntil() != null && account.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            throw new AccountLockedException("Account is locked until: " + account.getAccountLockedUntil());
        }

        if (account.getAccountLockedUntil() != null && account.getAccountLockedUntil().isBefore(LocalDateTime.now())) {
            account.setAccountLockedUntil(null);
            account.setFailedLoginAttempts(0);
            accountsRepository.save(account);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(account.getEmail())
                .password(account.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())))
                .accountExpired(false)
                .accountLocked(account.getStatus() == AccountStatus.LOCKED)
                .credentialsExpired(false)
                .disabled(account.getStatus() != AccountStatus.ACTIVE)
                .build();
    }
}


