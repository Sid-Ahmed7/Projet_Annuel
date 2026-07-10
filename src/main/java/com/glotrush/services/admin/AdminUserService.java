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
import com.glotrush.repositories.ai.AIGenerationLogRepository;
import com.glotrush.repositories.ChallengeParticipantsRepository;
import com.glotrush.repositories.ChallengeRepository;
import com.glotrush.repositories.FriendsRepository;
import com.glotrush.repositories.LessonSessionRepository;
import com.glotrush.repositories.PasswordResetTokenRepository;
import com.glotrush.repositories.PaymentHistoryRepository;
import com.glotrush.repositories.PushNotificationSubscriptionRepository;
import com.glotrush.repositories.SubscriptionRepository;
import com.glotrush.repositories.TopicReviewRepository;
import com.glotrush.repositories.UserLessonProgressRepository;
import com.glotrush.repositories.UserMistakeRepository;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.services.EmailService;
import com.glotrush.services.stripe.IStripService;
import com.glotrush.storage.FileStorageService;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService implements IAdminUserService {

    private final AccountsRepository accountsRepository;
    private final MessageSource messageSource;
    private final PasswordEncoder passwordEncoder;
    private final AdminUserBuilder adminUserBuilder;
    private final SubscriptionRepository subscriptionRepository;
    private final UserProfileRepository userProfileRepository;
    private final FileStorageService fileStorageService;
    private final AIGenerationLogRepository aiGenerationLogRepository;
    private final PushNotificationSubscriptionRepository pushNotificationSubscriptionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TopicReviewRepository topicReviewRepository;
    private final UserMistakeRepository userMistakeRepository;
    private final LessonSessionRepository lessonSessionRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final FriendsRepository friendsRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipantsRepository challengeParticipantsRepository;
    private final EmailService emailService;
    private final IStripService stripeService;

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

        subscriptionRepository.findByAccount_Id(accountId).ifPresent(subscription -> {
            if (subscription.getStripeSubscriptionId() == null || !subscription.getIsActive()) return;
            long refundAmount = stripeService.cancelWithProrationRefund(subscription.getStripeSubscriptionId(),subscription.getCurrentPeriodStart(),subscription.getCurrentPeriodEnd());
            if (refundAmount > 0) {
                emailService.sendRefundEmail(subscription.getAccount().getEmail(),subscription.getAccount().getUsername(),refundAmount);
            }
        });

        userProfileRepository.findByAccount_Id(accountId).ifPresent(profile -> {
            if (profile.getPhotoUrl() != null) {
                fileStorageService.delete("profiles/" + profile.getPhotoUrl());
                profile.setPhotoUrl(null);
                userProfileRepository.save(profile);
            }
        });

        pushNotificationSubscriptionRepository.deleteByAccount_Id(accountId);
        passwordResetTokenRepository.deleteByAccount_Id(accountId);

        account.setEmail("deleted_" + accountId + "@deleted.com");
        account.setFirstName("Deleted" + accountId);
        account.setLastName("Deleted" + accountId);
        account.setUsername("deleted_" + accountId.toString().substring(0, 8));
        account.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        account.setAuthKey(null);
        account.setStatus(AccountStatus.DELETED);
        accountsRepository.save(account);
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
