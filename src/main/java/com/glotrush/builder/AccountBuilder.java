package com.glotrush.builder;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.glotrush.dto.request.RegisterRequest;
import com.glotrush.dto.response.LoginResponse;
import com.glotrush.dto.response.RegisterResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.enumerations.AccountStatus;
import com.glotrush.enumerations.UserRole;

@Component
public class AccountBuilder {

    private final PasswordEncoder passwordEncoder;

    public AccountBuilder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public Accounts buildAccount(RegisterRequest request) {
        return Accounts.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .role(UserRole.USER)
                .status(AccountStatus.ACTIVE)
                .lastPasswordChange(LocalDateTime.now())
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

     public RegisterResponse buildRegisterResponse(Accounts account) {
        return RegisterResponse.builder()
                .userId(account.getId())
                .email(account.getEmail())
                .username(account.getUsername())
                .role(account.getRole().name())
                .message("Registration successful")
                .build();
    }

   public LoginResponse buildLoginResponse(Accounts account, String accessToken, String refreshToken, boolean requires2FA) {
        return LoginResponse.builder()
                .userId(account.getId())
                .email(account.getEmail())
                .username(account.getUsername())
                .role(account.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .requires2FA(requires2FA)
                .message(requires2FA ? "2FA code required" : "Login successful")
                .build();
    }
}