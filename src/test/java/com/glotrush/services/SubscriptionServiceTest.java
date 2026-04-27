package com.glotrush.services;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import com.glotrush.builder.SubscriptionBuilder;
import com.glotrush.config.TestMessageSourceConfig;
import com.glotrush.dto.response.SubscriptionResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Plan;
import com.glotrush.entities.Subscription;
import com.glotrush.enumerations.SubscriptionType;
import com.glotrush.enumerations.UserRole;
import com.glotrush.exceptions.SubscriptionAlreadyExistException;
import com.glotrush.exceptions.SubscriptionNotFoundException;
import com.glotrush.repositories.PlanRepository;
import com.glotrush.repositories.SubscriptionRepository;
import com.glotrush.services.subscription.SubscriptionService;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(classes = TestMessageSourceConfig.class)
@DisplayName("SubscriptionService Unit Tests")
class SubscriptionServiceTest {

    @Autowired
    private MessageSource messageSource;


    @Mock
    private SubscriptionBuilder subscriptionBuilder;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private EmailService emailService;

    private SubscriptionService subscriptionService;

    private Accounts accounts;
    private UUID accountId;
    private Subscription subscription;
    private UUID subscriptionId;
    private Plan freePlan;
    private Plan premiumPlan;

    @BeforeEach
    void setUp() {

        subscriptionService = new SubscriptionService(subscriptionRepository, subscriptionBuilder, planRepository, messageSource, emailService);

        accountId = UUID.randomUUID();
        subscriptionId = UUID.randomUUID();

        accounts = Accounts.builder()
                .id(accountId)
                .email("idontspeak@example.com")
                .username("IDONTSPEAKENGLISH")
                .firstName("Test")
                .lastName("ENGLISH")
                .role(UserRole.USER)
                .build();

        freePlan = Plan.builder()
                .id(UUID.randomUUID())
                .name("Free Plan")
                .description("You can use nothing AHAHAHAHA")
                .price(BigDecimal.ZERO)
                .subscriptionType(SubscriptionType.FREE)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
                premiumPlan = Plan.builder()
                .id(UUID.randomUUID())
                .name("Premium Plan")
                .description("Sorry for before, you can use all the features")
                .price(BigDecimal.valueOf(999.99))
                .subscriptionType(SubscriptionType.PREMIUM)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();


        subscription = Subscription.builder()
                .id(subscriptionId)
                .account(accounts)
                .plan(freePlan)
                .isActive(true)
                .startDate(LocalDateTime.now())
                .endDate(null)
                .build();

        
    }

    @Test
    @DisplayName("Should create a new subscription for an account")
    void shouldCreateSubscriptionForAnAccount() {

        when(planRepository.findBySubscriptionTypeAndIsActiveTrue(SubscriptionType.FREE)).thenReturn(Optional.of(freePlan));

        when(subscriptionRepository.existsByAccount_Id(accountId)).thenReturn(false);
        when(subscriptionBuilder.buildFreeSubscription(eq(accounts), any())).thenReturn(subscription);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        subscriptionService.createSubscriptionForUser(accounts);
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(subscriptionBuilder).buildFreeSubscription(eq(accounts), any());
    }

    @Test
    @DisplayName("Should not create a subscription if one already exists for the account")
    void shouldNotCreateSubscriptionIfAlreadyExists() {

        when(subscriptionRepository.existsByAccount_Id(accountId)).thenReturn(true);
        
        assertThatThrownBy(() -> subscriptionService.createSubscriptionForUser(accounts)).isInstanceOf(SubscriptionAlreadyExistException.class);
        verify(subscriptionRepository).existsByAccount_Id(accountId);
        verify(subscriptionBuilder, never()).buildFreeSubscription(eq(accounts), any());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should return the subscription for an account")
    void shouldReturnSubscriptionForAnAccount() {
        SubscriptionResponse subscriptionResponse = SubscriptionResponse.builder()
            .id(subscriptionId)
            .isActive(true)
            .startDate(LocalDateTime.now())
            .endDate(null)
            .build();
        
        when(subscriptionRepository.findByAccount_Id(accountId)).thenReturn(Optional.of(subscription));
        when(subscriptionBuilder.mapToSubscriptionResponse(subscription)).thenReturn(subscriptionResponse);
        
        SubscriptionResponse result = subscriptionService.getSubscription(accountId);
        
        assertThat(result).isNotNull();
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should throw SubscriptionNotFoundException when subscription does not exist for an account")
    void shouldThrowSubscriptionNotFoundException() {

        when(subscriptionRepository.findByAccount_Id(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.getSubscription(accountId)).isInstanceOf(SubscriptionNotFoundException.class);
    }


    @Test
    @DisplayName("Should expire all expired PREMIUM subscriptions and send emails")
    void shouldExpireAllExpiredPremiumSubscriptionsAndSendEmails() {

        when(planRepository.findBySubscriptionTypeAndIsActiveTrue(SubscriptionType.FREE)).thenReturn(Optional.of(freePlan));

        Subscription expiredFirstSubscription = Subscription.builder()
                .id(UUID.randomUUID())
                .account(accounts)
                .plan(premiumPlan)
                .isActive(true)
                .endDate(LocalDateTime.now().minusDays(1))
                .build();

        Accounts secondAccounts = Accounts.builder()
                .id(UUID.randomUUID())
                .email("idontspeakfrench@example.com")
                .username("IDONTSPEAKFRENCH")
                .firstName("Test")
                .lastName("FRENCH")
                .role(UserRole.USER)
                .build();

        Subscription expiredSecondSubscription = Subscription.builder()
                .id(UUID.randomUUID())
                .account(secondAccounts)
                .plan(premiumPlan)
                .isActive(true)
                .endDate(LocalDateTime.now().minusDays(2))
                .build();

        when(subscriptionRepository.findAllByPlanSubscriptionTypeAndIsActiveTrueAndEndDateBefore(eq(SubscriptionType.PREMIUM), any(LocalDateTime.class))).thenReturn(List.of(expiredFirstSubscription, expiredSecondSubscription));
        when(subscriptionRepository.findById(expiredFirstSubscription.getId())).thenReturn(Optional.of(expiredFirstSubscription));
        when(subscriptionRepository.findById(expiredSecondSubscription.getId())).thenReturn(Optional.of(expiredSecondSubscription));

        subscriptionService.checkAndChangeExpiredSubscriptions();

        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
        verify(emailService, times(2)).sendSubscriptionExpiredEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should do nothing when no expired subscriptions")
    void shouldDoNothingWhenNoExpiredSubscriptions() {
        when(subscriptionRepository.findAllByPlanSubscriptionTypeAndIsActiveTrueAndEndDateBefore(eq(SubscriptionType.PREMIUM), any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        subscriptionService.checkAndChangeExpiredSubscriptions();

        verify(subscriptionRepository, never()).save(any());
        verify(emailService, never()).sendSubscriptionExpiredEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should expire PREMIUM subscription successfully and send email")
    void shouldExpirePremiumSubscriptionSuccessfullyAndSendEmail() {
       
       
       when(planRepository.findBySubscriptionTypeAndIsActiveTrue(SubscriptionType.FREE)).thenReturn(Optional.of(freePlan));
       
        Subscription premiumSub = Subscription.builder()
                .id(subscriptionId)
                .account(accounts)
                .plan(premiumPlan)
                .isActive(true)
                .endDate(LocalDateTime.now().minusDays(1))
                .build();

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(premiumSub));

        subscriptionService.expireSingleSubscription(subscriptionId);

        verify(subscriptionRepository).save(any(Subscription.class));
        verify(emailService).sendSubscriptionExpiredEmail(accounts.getEmail(), accounts.getUsername());
    }

    @Test
    @DisplayName("Should throw exception when subscription not found for expiration")
    void shouldThrowExceptionWhenSubscriptionNotFoundForExpiration() {
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.expireSingleSubscription(subscriptionId))
                .isInstanceOf(SubscriptionNotFoundException.class);
    }

    @Test
    @DisplayName("Should not expire if subscription is not PREMIUM")
    void shouldNotExpireIfSubscriptionIsNotPremium() {
        subscription.setPlan(freePlan);

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

        subscriptionService.expireSingleSubscription(subscriptionId);

        verify(subscriptionRepository, never()).save(any());
        verify(emailService, never()).sendSubscriptionExpiredEmail(accounts.getEmail(), accounts.getUsername());
    }

    @Test
    @DisplayName("Should not expire if subscription is inactive")
    void shouldNotExpireIfSubscriptionIsInactive() {
        subscription.setPlan(premiumPlan);
        subscription.setIsActive(false);

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

        subscriptionService.expireSingleSubscription(subscriptionId);

        verify(subscriptionRepository, never()).save(any());
        verify(emailService, never()).sendSubscriptionExpiredEmail(accounts.getEmail(), accounts.getUsername());
    }

    @Test
    @DisplayName("Should not expire if end date is in future")
    void shouldNotExpireIfEndDateIsInFuture() {
        subscription.setPlan(premiumPlan);
        subscription.setIsActive(true);
        subscription.setEndDate(LocalDateTime.now().plusDays(10));

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

        subscriptionService.expireSingleSubscription(subscriptionId);

        verify(subscriptionRepository, never()).save(any());
        verify(emailService, never()).sendSubscriptionExpiredEmail(accounts.getEmail(), accounts.getUsername());
    }

    @Test
    @DisplayName("Should send email for subscriptions expiring soon")
    void shouldSendEmailForSubscriptionsExpiringSoon() {
        Subscription expiringSub = Subscription.builder()
                .id(subscriptionId)
                .account(accounts)
                .plan(premiumPlan)
                .isActive(true)
                .endDate(LocalDateTime.now().plusHours(12))
                .build();

        when(subscriptionRepository.findAllByPlanSubscriptionTypeAndIsActiveTrueAndEndDateBetween(eq(SubscriptionType.PREMIUM), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(expiringSub));

        subscriptionService.sendEmailWhenExpiringSoon();

        verify(emailService).sendSubscriptionExpiredSoonEmail(eq(accounts.getEmail()), eq(accounts.getUsername()), anyLong());
    }

    @Test
    @DisplayName("Should do nothing when no subscriptions expiring soon")
    void shouldDoNothingWhenNoSubscriptionsExpiringSoon() {
        when(subscriptionRepository.findAllByPlanSubscriptionTypeAndIsActiveTrueAndEndDateBetween(eq(SubscriptionType.PREMIUM), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        subscriptionService.sendEmailWhenExpiringSoon();

        verify(emailService, never()).sendSubscriptionExpiredSoonEmail(eq(accounts.getEmail()), eq(accounts.getUsername()), anyLong());
    }


    @Test
    @DisplayName("Should send reminder email")
    void shouldSendReminderEmail() {
        subscription.setEndDate(LocalDateTime.now().plusDays(1));

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

        subscriptionService.sendReminderEmailForExpiringSubscription(subscriptionId);

        verify(emailService).sendSubscriptionExpiredSoonEmail(eq(accounts.getEmail()), eq(accounts.getUsername()), anyLong());
    }

    @Test
    @DisplayName("Should throw exception when subscription not found for reminder")
    void shouldThrowExceptionWhenSubscriptionNotFoundForReminder() {
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.sendReminderEmailForExpiringSubscription(subscriptionId)).isInstanceOf(SubscriptionNotFoundException.class);
    }

    @Test
    @DisplayName("Should not send email if end date is null")
    void shouldNotSendEmailIfEndDateIsNull() {
        subscription.setEndDate(null);

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

        subscriptionService.sendReminderEmailForExpiringSubscription(subscriptionId);

        verify(emailService, never()).sendSubscriptionExpiredSoonEmail(eq(accounts.getEmail()), eq(accounts.getUsername()), anyLong());
    }

    
}
