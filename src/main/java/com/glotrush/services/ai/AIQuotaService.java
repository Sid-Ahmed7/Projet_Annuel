package com.glotrush.services.ai;

import com.glotrush.constants.AIUsageConstants;
import com.glotrush.dto.response.ai.AIQuotaResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Subscription;
import com.glotrush.enumerations.UserRole;
import com.glotrush.exceptions.AILimitExceededException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.SubscriptionRepository;
import com.glotrush.repositories.ai.AIGenerationLogRepository;
import com.glotrush.utils.LocaleUtils;
import lombok.RequiredArgsConstructor;
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

    @Transactional(readOnly = true)
    public void verifyAndConsumeAIQuota(UUID accountId) {
        Accounts account = accountsRepository.findById(accountId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.user.not_found", null, LocaleUtils.getCurrentLocale())));

        if (account.getRole() == UserRole.ADMIN){
            return;
        }

        Subscription subscription = subscriptionRepository.findByAccount_Id(accountId).orElse(null);
        int quota = resolveQuota(subscription);
        LocalDateTime periodStart = resolvePeriod(subscription, true);

        if (quota == AIUsageConstants.UNLIMITED_AI_QUOTA){
            return;
        }

        long usage = aiGenerationLogRepository.countByAccountIdAndCreatedAtAfter(accountId, periodStart);
        if (usage >= quota) {
            throw new AILimitExceededException(messageSource.getMessage("error.ai.quota.exceeded", null, LocaleUtils.getCurrentLocale()));
        }
    }

    @Transactional(readOnly = true)
    public AIQuotaResponse getQuotaDetails(UUID accountId) {
        Accounts account = accountsRepository.findById(accountId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.user.not_found", null, LocaleUtils.getCurrentLocale())));

        if (account.getRole() == UserRole.ADMIN) {
            return AIQuotaResponse.builder()
                .maxQuota(AIUsageConstants.UNLIMITED_AI_QUOTA).currentUsage(0).remainingQuota(AIUsageConstants.UNLIMITED_AI_QUOTA).periodEnd(null)
                .build();
        }

        Subscription subscription = subscriptionRepository.findByAccount_Id(accountId).orElse(null);
        int quota = resolveQuota(subscription);
        LocalDateTime periodStart = resolvePeriod(subscription, true);
        LocalDateTime periodEnd = resolvePeriod(subscription, false);

        long usage = aiGenerationLogRepository.countByAccountIdAndCreatedAtAfter(accountId, periodStart);
        long remaining = (quota == AIUsageConstants.UNLIMITED_AI_QUOTA) ? AIUsageConstants.UNLIMITED_AI_QUOTA : Math.max(0, quota - usage);

        return AIQuotaResponse.builder()
                .maxQuota(quota)
                .currentUsage(usage)
                .remainingQuota(remaining)
                .periodEnd(periodEnd)
                .build();
    }

    private int resolveQuota(Subscription subscription) {
        if (subscription == null || subscription.getPlan() == null){
            return AIUsageConstants.DEFAULT_AI_QUOTA;
        }
        Integer quota = subscription.getPlan().getAiQuota();
        return quota != null ? quota : AIUsageConstants.DEFAULT_AI_QUOTA;
    }

    private LocalDateTime resolvePeriod(Subscription subscription, boolean start) {
    if (subscription != null) {
        LocalDateTime period = start ? subscription.getCurrentPeriodStart() : subscription.getCurrentPeriodEnd();
        if (period != null) {
            return period;
        }
    }
    LocalDateTime firstDayOfMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
    return start ? firstDayOfMonth : firstDayOfMonth.plusMonths(1).minusSeconds(1);
}
}
