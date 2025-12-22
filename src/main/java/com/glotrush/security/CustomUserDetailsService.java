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
        Accounts user = accountsRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            throw new AccountLockedException("Account is locked until: " + user.getAccountLockedUntil());
        }

        if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isBefore(LocalDateTime.now())) {
            user.setAccountLockedUntil(null);
            user.setFailedLoginAttempts(0);
            accountsRepository.save(user);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .accountExpired(false)
                .accountLocked(user.getStatus() == AccountStatus.LOCKED)
                .credentialsExpired(false)
                .disabled(user.getStatus() != AccountStatus.ACTIVE)
                .build();
    }
}


