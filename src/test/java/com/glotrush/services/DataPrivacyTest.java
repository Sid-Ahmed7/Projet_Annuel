package com.glotrush.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glotrush.entities.*;
import com.glotrush.enumerations.AccountStatus;
import com.glotrush.enumerations.UserRole;
import com.glotrush.exceptions.MismatchCodeException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.mapping.ExportDataMapper;
import com.glotrush.repositories.*;
import com.glotrush.services.dataPrivacy.DataPrivacyService;
import com.glotrush.services.dataPrivacy.IDataPrivacyService;
import com.glotrush.services.stripe.IStripService;
import com.glotrush.storage.FileStorageService;
import com.glotrush.utils.GenerateRandomCodeUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.glotrush.config.TestMessageSourceConfig;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(classes = TestMessageSourceConfig.class)
@DisplayName("DataPrivacyService Unit Tests")
class DataPrivacyServiceTest {

    @Mock 
    private AccountsRepository accountsRepository;
    
    @Mock 
    private ExportDataMapper exportDataMapper;
    
    @Mock 
    private UserProfileRepository userProfileRepository;
    
    @Mock 
    private UserLanguageRepository userLanguageRepository;
    
    @Mock 
    private UserProgressRepository userProgressRepository;
    
    @Mock 
    private UserLessonProgressRepository userLessonProgressRepository;
    
    @Mock 
    private LessonSessionRepository lessonSessionRepository;
    
    @Mock 
    private UserMistakeRepository userMistakeRepository;
    
    @Mock 
    private FriendsRepository friendsRepository;
    
    @Mock 
    private ChallengeRepository challengeRepository;
    
    @Mock 
    private SubscriptionRepository subscriptionRepository;
    
    @Mock 
    private PaymentHistoryRepository paymentHistoryRepository;
    
    
    @Mock 
    private PushNotificationSubscriptionRepository pushNotificationSubscriptionRepository;
    
    @Mock 
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Mock 
    private TopicReviewRepository topicReviewRepository;
    
    @Mock 
    private FileStorageService fileStorageService;
    
    @Mock 
    private IStripService stripeService;
    
    @Mock 
    private EmailService emailService;
    
    @Mock 
    private AccountDeletionCodeRepository accountDeletionCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Autowired 
    private MessageSource messageSource;

    private IDataPrivacyService service;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Accounts account;
    private UUID accountId;
    private String code;
    
    @BeforeEach
    void setUp() {
    service = new DataPrivacyService(accountsRepository, exportDataMapper, userProfileRepository, messageSource, userLanguageRepository, userProgressRepository, userLessonProgressRepository, lessonSessionRepository,
        userMistakeRepository, passwordEncoder, friendsRepository, challengeRepository, subscriptionRepository, paymentHistoryRepository,
        pushNotificationSubscriptionRepository, passwordResetTokenRepository, topicReviewRepository, fileStorageService, objectMapper, stripeService, emailService, accountDeletionCodeRepository
    );

        accountId = UUID.randomUUID();
       
        account = Accounts.builder()
            .id(accountId)
            .email("sukuna@gmail.com")
            .username("sukuna")
            .password("SukunaGojo123@?")
            .firstName("Ryomen")
            .lastName("Sukuna")
            .role(UserRole.USER)
            .failedLoginAttempts(0)
            .lastPasswordChange(LocalDateTime.now())
            .build();

        code = GenerateRandomCodeUtils.generateRandomCode();

    }



    @Test
    @DisplayName("Should export user data ")
    void shouldExportUserData() throws Exception {
  
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(userLanguageRepository.findByAccount_Id(accountId)).thenReturn(List.of());
        when(userProgressRepository.findByAccount_Id(accountId)).thenReturn(List.of());
        when(userLessonProgressRepository.findByAccount_Id(accountId)).thenReturn(List.of());
        when(lessonSessionRepository.findByAccount_IdOrderByCompletedAtDesc(accountId)).thenReturn(List.of());
        when(userMistakeRepository.findByAccount_IdAndIsResolvedFalseOrderByCreatedAtAsc(accountId)).thenReturn(List.of());
        when(friendsRepository.findAcceptedRequests(accountId)).thenReturn(List.of());
        when(challengeRepository.findAllByAccountId(accountId)).thenReturn(List.of());
        when(topicReviewRepository.findByAccount_Id(accountId)).thenReturn(List.of());
        when(paymentHistoryRepository.findAllByAccount_IdOrderByCreatedAtDesc(accountId)).thenReturn(List.of());
        when(userProfileRepository.findByAccount_Id(accountId)).thenReturn(Optional.empty());
        when(subscriptionRepository.findByAccount_Id(accountId)).thenReturn(Optional.empty());

        byte[] result = service.exportUserData(accountId);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        verify(exportDataMapper).mapAccount(account);
        verify(exportDataMapper).mapLanguages(any());
        verify(exportDataMapper).mapProgressList(any());
        verify(exportDataMapper).mapLessonSessions(any());
        verify(exportDataMapper).mapUserMistakes(any());
        verify(exportDataMapper).mapFriends(any(), eq(accountId));
        verify(exportDataMapper).mapChallenges(any());
        verify(exportDataMapper).mapTopicReviews(any());
        verify(exportDataMapper).mapPaymentHistoryList(any());
    }

    @Test
    @DisplayName("Should map profile when present")
    void shouldMapProfileWhenPresent() throws Exception {

        UserProfile profile = UserProfile.builder().build();

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(userProfileRepository.findByAccount_Id(accountId)).thenReturn(Optional.of(profile));
        when(userLanguageRepository.findByAccount_Id(accountId)).thenReturn(List.of());
        when(userProgressRepository.findByAccount_Id(accountId)).thenReturn(List.of());
        when(userLessonProgressRepository.findByAccount_Id(accountId)).thenReturn(List.of());
        when(lessonSessionRepository.findByAccount_IdOrderByCompletedAtDesc(accountId)).thenReturn(List.of());
        when(userMistakeRepository.findByAccount_IdAndIsResolvedFalseOrderByCreatedAtAsc(accountId)).thenReturn(List.of());
        when(friendsRepository.findAcceptedRequests(accountId)).thenReturn(List.of());
        when(challengeRepository.findAllByAccountId(accountId)).thenReturn(List.of());
        when(topicReviewRepository.findByAccount_Id(accountId)).thenReturn(List.of());
        when(paymentHistoryRepository.findAllByAccount_IdOrderByCreatedAtDesc(accountId)).thenReturn(List.of());
        when(subscriptionRepository.findByAccount_Id(accountId)).thenReturn(Optional.empty());

        service.exportUserData(accountId);

        verify(exportDataMapper).mapProfile(profile);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if account does not exist")
    void shouldThrowWhenAccountNotFoundOnExport() {
        when(accountsRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.exportUserData(accountId)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Should generate account deletion code and send email")
    void shouldGenerateDeletionCodeAndSendEmail() {  
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(account));
        service.generateAccountDeletionCode(accountId);

        verify(accountDeletionCodeRepository).revokeAllByAccountId(accountId);
        verify(accountDeletionCodeRepository).save(any(AccountDeletionCode.class));
        verify(emailService).sendAccountDeletionEmail(eq("sukuna@gmail.com"), eq("sukuna"), any());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if account does not exist")
    void shouldThrowWhenAccountNotFoundOnGenerateCode() {
        when(accountsRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateAccountDeletionCode(accountId)).isInstanceOf(UserNotFoundException.class);
    }


    @Test
    @DisplayName("Should anonymize account data on deletion")
    void shouldDeleteAllAccountData() {
        AccountDeletionCode userCode = AccountDeletionCode.builder()
            .account(account)
            .code(code)
            .isUsed(false)
            .build();

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountDeletionCodeRepository.findValidCode(eq(code), any())).thenReturn(Optional.of(userCode));
        when(subscriptionRepository.findByAccount_Id(accountId)).thenReturn(Optional.empty());
        when(userProfileRepository.findByAccount_Id(accountId)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        service.deleteAccount(accountId, code);

        verify(accountDeletionCodeRepository).save(userCode);
        verify(pushNotificationSubscriptionRepository).deleteByAccount_Id(accountId);
        verify(passwordResetTokenRepository).deleteByAccount_Id(accountId);
        verify(accountsRepository).save(account);

        assertThat(account.getEmail()).startsWith("deleted_");
        assertThat(account.getUsername()).startsWith("deleted_");
        assertThat(account.getStatus()).isEqualTo(AccountStatus.DELETED);

        verify(accountsRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should throw MismatchCodeException if code is invalid")
    void shouldThrowWhenCodeIsInvalid() {
        String invalidCode = "SATORU";

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountDeletionCodeRepository.findValidCode(eq(invalidCode), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteAccount(accountId, invalidCode)).isInstanceOf(MismatchCodeException.class);
    }
}