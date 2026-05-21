package com.glotrush.services.ai;

import com.glotrush.config.TestMessageSourceConfig;
import com.glotrush.dto.response.ai.AIQuotaResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Plan;
import com.glotrush.entities.Subscription;
import com.glotrush.enumerations.SubscriptionType;
import com.glotrush.enumerations.UserRole;
import com.glotrush.exceptions.AILimitExceededException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.SubscriptionRepository;
import com.glotrush.repositories.ai.AIGenerationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AIQuotaService Unit Tests")
class AIQuotaServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private AIGenerationLogRepository aiGenerationLogRepository;

    private AIQuotaService aiQuotaService;

    private UUID accountId;
    private Accounts adminAccount;
    private Accounts userAccount;
    private Subscription freeSubscription;
    private Subscription premiumSubscription;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        
        aiQuotaService = new AIQuotaService(accountsRepository, subscriptionRepository, aiGenerationLogRepository, messageSource);
        ReflectionTestUtils.setField(aiQuotaService, "freeLimit", 5);
        ReflectionTestUtils.setField(aiQuotaService, "premiumLimit", 100);

        lenient().when(messageSource.getMessage(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        adminAccount = Accounts.builder()
                .id(accountId)
                .email("admin@glotrush.com")
                .role(UserRole.ADMIN)
                .build();

        userAccount = Accounts.builder()
                .id(accountId)
                .email("user@glotrush.com")
                .role(UserRole.USER)
                .build();

        Plan freePlan = Plan.builder()
                .subscriptionType(SubscriptionType.FREE)
                .build();

        Plan premiumPlan = Plan.builder()
                .subscriptionType(SubscriptionType.PREMIUM)
                .build();

        freeSubscription = Subscription.builder()
                .account(userAccount)
                .plan(freePlan)
                .isActive(true)
                .build();

        premiumSubscription = Subscription.builder()
                .account(userAccount)
                .plan(premiumPlan)
                .isActive(true)
                .currentPeriodStart(LocalDateTime.now().minusDays(5))
                .currentPeriodEnd(LocalDateTime.now().plusDays(25))
                .build();
    }

    @Test
    @DisplayName("Should allow unlimited access for ADMIN without calling count queries")
    void shouldAllowUnlimitedAccessForAdmin() {
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(adminAccount));

        aiQuotaService.verifyAndConsumeAIQuota(accountId);

        verify(aiGenerationLogRepository, never()).countByAccountIdAndCreatedAtAfter(any(), any());
    }

    @Test
    @DisplayName("Should throw exception if user is not found")
    void shouldThrowExceptionIfUserNotFound() {
        when(accountsRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiQuotaService.verifyAndConsumeAIQuota(accountId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Should allow USER FREE if usage is below limit")
    void shouldAllowUserFreeBelowLimit() {
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(userAccount));
        when(subscriptionRepository.findByAccount_Id(accountId)).thenReturn(Optional.of(freeSubscription));
        when(aiGenerationLogRepository.countByAccountIdAndCreatedAtAfter(eq(accountId), any(LocalDateTime.class))).thenReturn(3L);

        aiQuotaService.verifyAndConsumeAIQuota(accountId);

        verify(aiGenerationLogRepository).countByAccountIdAndCreatedAtAfter(eq(accountId), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should block USER FREE if usage is equal or above limit")
    void shouldBlockUserFreeAboveLimit() {
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(userAccount));
        when(subscriptionRepository.findByAccount_Id(accountId)).thenReturn(Optional.of(freeSubscription));
        when(aiGenerationLogRepository.countByAccountIdAndCreatedAtAfter(eq(accountId), any(LocalDateTime.class))).thenReturn(5L);

        assertThatThrownBy(() -> aiQuotaService.verifyAndConsumeAIQuota(accountId))
                .isInstanceOf(AILimitExceededException.class);
    }

    @Test
    @DisplayName("Should allow USER PREMIUM if usage is below limit")
    void shouldAllowUserPremiumBelowLimit() {
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(userAccount));
        when(subscriptionRepository.findByAccount_Id(accountId)).thenReturn(Optional.of(premiumSubscription));
        when(aiGenerationLogRepository.countByAccountIdAndCreatedAtAfter(eq(accountId), any(LocalDateTime.class))).thenReturn(45L);

        aiQuotaService.verifyAndConsumeAIQuota(accountId);

        verify(aiGenerationLogRepository).countByAccountIdAndCreatedAtAfter(eq(accountId), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should block USER PREMIUM if usage is equal or above limit")
    void shouldBlockUserPremiumAboveLimit() {
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(userAccount));
        when(subscriptionRepository.findByAccount_Id(accountId)).thenReturn(Optional.of(premiumSubscription));
        when(aiGenerationLogRepository.countByAccountIdAndCreatedAtAfter(eq(accountId), any(LocalDateTime.class))).thenReturn(100L);

        assertThatThrownBy(() -> aiQuotaService.verifyAndConsumeAIQuota(accountId))
                .isInstanceOf(AILimitExceededException.class);
    }

    @Test
    @DisplayName("Should return unlimited quota details for ADMIN")
    void shouldReturnUnlimitedQuotaDetailsForAdmin() {
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(adminAccount));

        AIQuotaResponse response = aiQuotaService.getQuotaDetails(accountId);

        assertThat(response.getMaxQuota()).isEqualTo(-1);
        assertThat(response.getRemainingQuota()).isEqualTo(-1);
        assertThat(response.getCurrentUsage()).isEqualTo(0);
        assertThat(response.getPeriodEnd()).isNull();
    }

    @Test
    @DisplayName("Should return correct quota details for USER FREE")
    void shouldReturnCorrectQuotaDetailsForUserFree() {
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(userAccount));
        when(subscriptionRepository.findByAccount_Id(accountId)).thenReturn(Optional.of(freeSubscription));
        when(aiGenerationLogRepository.countByAccountIdAndCreatedAtAfter(eq(accountId), any(LocalDateTime.class))).thenReturn(2L);

        AIQuotaResponse response = aiQuotaService.getQuotaDetails(accountId);

        assertThat(response.getMaxQuota()).isEqualTo(5);
        assertThat(response.getCurrentUsage()).isEqualTo(2);
        assertThat(response.getRemainingQuota()).isEqualTo(3);
        assertThat(response.getPeriodEnd()).isNotNull();
    }

    @Test
    @DisplayName("Should return correct quota details for USER PREMIUM")
    void shouldReturnCorrectQuotaDetailsForUserPremium() {
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(userAccount));
        when(subscriptionRepository.findByAccount_Id(accountId)).thenReturn(Optional.of(premiumSubscription));
        when(aiGenerationLogRepository.countByAccountIdAndCreatedAtAfter(eq(accountId), any(LocalDateTime.class))).thenReturn(15L);

        AIQuotaResponse response = aiQuotaService.getQuotaDetails(accountId);

        assertThat(response.getMaxQuota()).isEqualTo(100);
        assertThat(response.getCurrentUsage()).isEqualTo(15);
        assertThat(response.getRemainingQuota()).isEqualTo(85);
        assertThat(response.getPeriodEnd()).isEqualTo(premiumSubscription.getCurrentPeriodEnd());
    }
}
