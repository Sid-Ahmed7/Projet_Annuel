package com.glotrush.scheduler.services.review;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.glotrush.constants.ApiConstants;
import com.glotrush.constants.TopicConstants;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.TopicReview;
import com.glotrush.enumerations.ReviewStatus;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.TopicReviewRepository;
import com.glotrush.services.notifications.NotificationService;
import com.glotrush.utils.LocaleUtils;

import java.util.UUID;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class ModerationScheduler implements IModerationScheduler {
    
    private final TopicReviewRepository topicReviewRepository;
    private final AccountsRepository accountsRepository;
    private final NotificationService notificationService;
    private final MessageSource messageSource;

    @Scheduled(fixedDelay = ApiConstants.FIXED_DELAY)
    @Override
    @Transactional
    public void autoRejectExpiredReviews() {
        LocalDateTime lastTwoHour = LocalDateTime.now().minusHours(2);
        List<TopicReview> expired = topicReviewRepository.findByStatusAndUpdatedAtBefore(ReviewStatus.PENDING, lastTwoHour);
        if(expired.isEmpty()) {
            log.info("No expired reviews found");
            return;
        }

        log.info("Found {} expired reviews", expired.size());

        for(TopicReview review : expired) {
            review.setStatus(ReviewStatus.REJECTED);
            topicReviewRepository.save(review);
            log.info("Review {} rejected", review.getId());
            Accounts account = review.getAccount();
            int newCount = account.getRejectedReviewCount() + 1;
            account.setRejectedReviewCount(newCount);

            UUID accountId = account.getId();
            String eventType;
            String eventMsg;

            if(newCount >= TopicConstants.LIMIT_REJECTED_REVIEWS) {
                account.setIsBannedOfReview(true);
                accountsRepository.save(account);
                eventType = "REVIEW_BANNED";
                eventMsg = messageSource.getMessage("error.review.banned", null, LocaleUtils.getCurrentLocale());
                log.info("Account {} banned from reviewing", account.getId());
            } else {
                accountsRepository.save(account);
                eventType = "REVIEW_AUTO_REJECTED";
                eventMsg = messageSource.getMessage("info.review.rejected", null, LocaleUtils.getCurrentLocale());
                log.info("Account {} has now {} rejected reviews", account.getId(), newCount);
            }

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notificationService.sendNotification(accountId, eventType, eventMsg);
                }
            });
        }
    }
    
    
}
