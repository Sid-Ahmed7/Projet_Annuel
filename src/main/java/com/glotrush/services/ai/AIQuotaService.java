package com.glotrush.services.ai;

import com.glotrush.dto.response.ai.AIQuotaResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Subscription;
import com.glotrush.enumerations.SubscriptionType;
import com.glotrush.enumerations.UserRole;
import com.glotrush.exceptions.AILimitExceededException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.SubscriptionRepository;
import com.glotrush.repositories.ai.AIGenerationLogRepository;
import com.glotrush.utils.LocaleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AIQuotaService {

    private final AccountsRepository accountsRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AIGenerationLogRepository aiGenerationLogRepository;
    private final MessageSource messageSource;

    @Value("${ai.quota.free-limit:5}")
    private int freeLimit;

    @Value("${ai.quota.premium-limit:100}")
    private int premiumLimit;

    @Transactional(readOnly = true)
    public void verifyAndConsumeAIQuota(UUID accountId) {
        Accounts account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.user.not_found", null, LocaleUtils.getCurrentLocale())));

        if (account.getRole() == UserRole.ADMIN) {
            return;
        }

        Subscription subscription = subscriptionRepository.findByAccount_Id(accountId).orElse(null);
        SubscriptionType type = (subscription != null && subscription.getPlan() != null) 
                ? subscription.getPlan().getSubscriptionType() 
                : SubscriptionType.FREE;

        if (type == SubscriptionType.FREE) {
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
            long usage = aiGenerationLogRepository.countByAccountIdAndCreatedAtAfter(accountId, startOfMonth);
            
            if (usage >= freeLimit) {
                throw new AILimitExceededException(messageSource.getMessage("error.ai.quota.exceeded", null, LocaleUtils.getCurrentLocale()));
            }
        } else if (type == SubscriptionType.PREMIUM) {
            LocalDateTime periodStart = (subscription.getCurrentPeriodStart() != null) 
                    ? subscription.getCurrentPeriodStart() 
                    : LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
            
            long usage = aiGenerationLogRepository.countByAccountIdAndCreatedAtAfter(accountId, periodStart);
            
            if (premiumLimit != -1 && usage >= premiumLimit) {
                throw new AILimitExceededException(messageSource.getMessage("error.ai.quota.exceeded", null, LocaleUtils.getCurrentLocale()));
            }
        }
    }

    @Transactional(readOnly = true)
    public AIQuotaResponse getQuotaDetails(UUID accountId) {
        Accounts account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.user.not_found", null, LocaleUtils.getCurrentLocale())));

        if (account.getRole() == UserRole.ADMIN) {
            return AIQuotaResponse.builder()
                    .maxQuota(-1)
                    .currentUsage(0)
                    .remainingQuota(-1)
                    .periodEnd(null)
                    .build();
        }

        Subscription subscription = subscriptionRepository.findByAccount_Id(accountId).orElse(null);
        SubscriptionType type = (subscription != null && subscription.getPlan() != null) 
                ? subscription.getPlan().getSubscriptionType() 
                : SubscriptionType.FREE;

        if (type == SubscriptionType.FREE) {
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
            LocalDateTime endOfMonth = LocalDateTime.now().withDayOfMonth(1).plusMonths(1).toLocalDate().atStartOfDay().minusSeconds(1);
            long usage = aiGenerationLogRepository.countByAccountIdAndCreatedAtAfter(accountId, startOfMonth);
            
            return AIQuotaResponse.builder()
                    .maxQuota(freeLimit)
                    .currentUsage(usage)
                    .remainingQuota(Math.max(0, freeLimit - usage))
                    .periodEnd(endOfMonth)
                    .build();
        } else {
            LocalDateTime periodStart = (subscription != null && subscription.getCurrentPeriodStart() != null) 
                    ? subscription.getCurrentPeriodStart() 
                    : LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
            
            LocalDateTime periodEnd = (subscription != null && subscription.getCurrentPeriodEnd() != null) 
                    ? subscription.getCurrentPeriodEnd() 
                    : LocalDateTime.now().withDayOfMonth(1).plusMonths(1).toLocalDate().atStartOfDay().minusSeconds(1);
            
            long usage = aiGenerationLogRepository.countByAccountIdAndCreatedAtAfter(accountId, periodStart);
            long remaining = (premiumLimit == -1) ? -1 : Math.max(0, premiumLimit - usage);
            
            return AIQuotaResponse.builder()
                    .maxQuota(premiumLimit)
                    .currentUsage(usage)
                    .remainingQuota(remaining)
                    .periodEnd(periodEnd)
                    .build();
        }
    }
}
