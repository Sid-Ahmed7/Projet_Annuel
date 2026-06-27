package com.glotrush.services.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.glotrush.dto.response.AdminUserListResponse;
import com.glotrush.builder.AdminUserBuilder;
import com.glotrush.dto.response.AdminUserDetailsResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.enumerations.AccountStatus;
import com.glotrush.enumerations.UserRole;
import com.glotrush.exceptions.ResourceNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService implements IAdminUserService {

    private final AccountsRepository accountsRepository;
    private final MessageSource messageSource;
    private final PasswordEncoder passwordEncoder;
    private final AdminUserBuilder adminUserBuilder;

    @Override
    public AdminUserListResponse getUsers(String search, Integer page, Integer size) {
        Page<Accounts> accounts = accountsRepository.findUsersWithSearch(UserRole.USER,(search != null && !search.isBlank()) ? search : null,PageRequest.of(page, size));

        List<AdminUserDetailsResponse> users = accounts.getContent().stream().map(adminUserBuilder::buildUserDetailsResponse).toList();

        return AdminUserListResponse.builder()
            .users(users)
            .totalUsers(accounts.getTotalElements())
            .currentPage(page)
            .pageSize(size)
            .build();
    }

    @Override
    public void updateUserStatus(UUID accountId, AccountStatus status) {
        Accounts account = findUser(accountId);
        account.setStatus(status);
        accountsRepository.save(account);
    }

    @Override
    public void unlockUser(UUID accountId) {
        Accounts account = findUser(accountId);
        account.setStatus(AccountStatus.ACTIVE);
        account.setFailedLoginAttempts(0);
        account.setAccountLockedUntil(null);
        accountsRepository.save(account);
    }

    @Override
    public void deleteUser(UUID accountId) {
        Accounts account = findUser(accountId);
        accountsRepository.delete(account);
    }

    @Override
    public void resetUserPassword(UUID accountId, String newPassword) {
        Accounts account = findUser(accountId);
        account.setPassword(passwordEncoder.encode(newPassword));
        account.setLastPasswordChange(LocalDateTime.now());
        accountsRepository.save(account);
    }

    private Accounts findUser(UUID accountId) {
        return accountsRepository.findById(accountId).orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, LocaleUtils.getCurrentLocale())));
    }
}
