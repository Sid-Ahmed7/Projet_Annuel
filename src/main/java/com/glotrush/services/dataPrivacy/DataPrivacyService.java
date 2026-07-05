package com.glotrush.services.dataPrivacy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.glotrush.entities.AccountDeletionCode;
import com.glotrush.entities.Accounts;
import com.glotrush.exceptions.MismatchCodeException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.mapping.ExportDataMapper;
import com.glotrush.repositories.AccountDeletionCodeRepository;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.ChallengeParticipantsRepository;
import com.glotrush.repositories.ChallengeRepository;
import com.glotrush.repositories.FriendsRepository;
import com.glotrush.repositories.LessonSessionRepository;
import com.glotrush.repositories.PasswordResetTokenRepository;
import com.glotrush.repositories.PaymentHistoryRepository;
import com.glotrush.repositories.PushNotificationSubscriptionRepository;
import com.glotrush.repositories.SubscriptionRepository;
import com.glotrush.repositories.TopicReviewRepository;
import com.glotrush.repositories.UserLanguageRepository;
import com.glotrush.repositories.UserLessonProgressRepository;
import com.glotrush.repositories.UserMistakeRepository;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.repositories.UserProgressRepository;
import com.glotrush.repositories.ai.AIGenerationLogRepository;
import com.glotrush.services.EmailService;
import com.glotrush.services.stripe.IStripService;
import com.glotrush.storage.FileStorageService;
import com.glotrush.utils.GenerateRandomCode;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DataPrivacyService implements IDataPrivacyService {

    private final AccountsRepository accountsRepository;
    private final ExportDataMapper exportDataMapper;
    private final UserProfileRepository userProfileRepository;
    private final MessageSource messageSource;
    private final UserLanguageRepository userLanguageRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;
    private final LessonSessionRepository lessonSessionRepository;
    private final UserMistakeRepository userMistakeRepository;
    private final FriendsRepository friendsRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipantsRepository challengeParticipantsRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final AIGenerationLogRepository aiGenerationLogRepository;
    private final PushNotificationSubscriptionRepository pushNotificationSubscriptionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TopicReviewRepository topicReviewRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;
    private final IStripService stripeService;
    private final EmailService emailService;
    private final AccountDeletionCodeRepository accountDeletionCodeRepository;

    @Override
    public byte[] exportUserData(UUID accountId) throws Exception {
        Accounts account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.user.not_found", null, LocaleUtils.getCurrentLocale())));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("account", exportDataMapper.mapAccount(account));
        userProfileRepository.findByAccount_Id(accountId).ifPresent(p -> data.put("profile", exportDataMapper.mapProfile(p)));
        data.put("languages", exportDataMapper.mapLanguages(userLanguageRepository.findByAccount_Id(accountId)));
        data.put("progress", exportDataMapper.mapProgressList(userProgressRepository.findByAccount_Id(accountId)));
        data.put("lessonProgress", exportDataMapper.mapLessonProgressList(userLessonProgressRepository.findByAccount_Id(accountId)));
        data.put("sessions", exportDataMapper.mapLessonSessions(lessonSessionRepository.findByAccount_IdOrderByCompletedAtDesc(accountId)));
        data.put("mistakes", exportDataMapper.mapUserMistakes(userMistakeRepository.findByAccount_IdAndIsResolvedFalseOrderByCreatedAtAsc(accountId)));
        data.put("friends", exportDataMapper.mapFriends(friendsRepository.findAcceptedRequests(accountId), accountId));
        data.put("challenges", exportDataMapper.mapChallenges(challengeRepository.findAllByAccountId(accountId)));
        data.put("reviews", exportDataMapper.mapTopicReviews(topicReviewRepository.findByAccount_Id(accountId)));
        subscriptionRepository.findByAccount_Id(accountId).ifPresent(s -> data.put("subscription", exportDataMapper.mapSubscription(s)));
        data.put("payments", exportDataMapper.mapPaymentHistoryList(paymentHistoryRepository.findAllByAccount_IdOrderByCreatedAtDesc(accountId)));

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(data);
    }

    @Override
    public void generateAccountDeletionCode(UUID accountId) {
        Accounts account = accountsRepository.findById(accountId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.user.not_found", null, LocaleUtils.getCurrentLocale())));

        accountDeletionCodeRepository.revokeAllByAccountId(accountId);

        String code = GenerateRandomCode.generateRandomCode();

        AccountDeletionCode deletionCode = AccountDeletionCode.builder()
                .account(account)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
        accountDeletionCodeRepository.save(deletionCode);

        emailService.sendAccountDeletionEmail(account.getEmail(), account.getUsername(), code);
    }

    @Override
    public void deleteAccount(UUID accountId, String code) {
        Accounts account = accountsRepository.findById(accountId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.user.not_found", null, LocaleUtils.getCurrentLocale())));

        AccountDeletionCode deletionCode = accountDeletionCodeRepository.findValidCode(code, LocalDateTime.now()).filter(c -> c.getAccount().getId().equals(accountId)).orElseThrow(() -> new MismatchCodeException(messageSource.getMessage("error.deletion.code.invalid", null, LocaleUtils.getCurrentLocale())));

        deletionCode.setIsUsed(true);
        accountDeletionCodeRepository.save(deletionCode);

        subscriptionRepository.findByAccount_Id(accountId).ifPresent(subscription -> {
            if (subscription.getStripeSubscriptionId() == null || !subscription.getIsActive()) return;
            long refundAmount = stripeService.cancelWithProrationRefund(
                    subscription.getStripeSubscriptionId(),
                    subscription.getCurrentPeriodStart(),
                    subscription.getCurrentPeriodEnd());
            if (refundAmount > 0) {
                emailService.sendRefundEmail(account.getEmail(), account.getUsername(), refundAmount);
            }
        });

        userProfileRepository.findByAccount_Id(accountId).ifPresent(profile -> {
            if (profile.getPhotoUrl() != null) {
                fileStorageService.delete("profiles/" + profile.getPhotoUrl());
            }
        });

        aiGenerationLogRepository.deleteByAccountId(accountId);
        pushNotificationSubscriptionRepository.deleteByAccount_Id(accountId);
        passwordResetTokenRepository.deleteByAccount_Id(accountId);
        accountDeletionCodeRepository.deleteByAccount_Id(accountId);
        topicReviewRepository.deleteByAccount_Id(accountId);
        userMistakeRepository.deleteByAccount_Id(accountId);
        lessonSessionRepository.deleteByAccount_Id(accountId);
        userLessonProgressRepository.deleteByAccount_Id(accountId);
        paymentHistoryRepository.deleteByAccount_Id(accountId);
        subscriptionRepository.deleteByAccount_Id(accountId);
        friendsRepository.deleteAllByAccountId(accountId);
        challengeRepository.nullifyChallenged(accountId);
        challengeParticipantsRepository.deleteByAccount_Id(accountId);
        challengeRepository.deleteByChallenger_Id(accountId);
        accountsRepository.deleteById(accountId);
    }
}