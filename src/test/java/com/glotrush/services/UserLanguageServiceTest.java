package com.glotrush.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

import com.glotrush.builder.UserLanguageBuilder;
import com.glotrush.builder.UserProfileBuilder;
import com.glotrush.config.TestMessageSourceConfig;
import com.glotrush.dto.request.AddUserLanguageRequest;
import com.glotrush.dto.request.UpdateUserLanguageRequest;
import com.glotrush.dto.response.UserLanguageResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Language;
import com.glotrush.entities.UserLanguage;
import com.glotrush.enumerations.LanguageType;
import com.glotrush.enumerations.ProficiencyLevel;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.LanguageRepository;
import com.glotrush.repositories.UserLanguageRepository;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.services.languages.UserLanguageService;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(classes = TestMessageSourceConfig.class)
@DisplayName("UserLanguageService Unit Tests")
class UserLanguageServiceTest {
    @Mock
    private UserLanguageRepository userLanguageRepository;

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private UserLanguageBuilder userLanguageBuilder;

    @Autowired
    private MessageSource messageSource;
    
    @Mock
    UserProfileRepository userProfileRepository;
    @Mock
    private UserProfileBuilder userProfileBuilder;

    private UserLanguageService userLanguageService;

    private UUID accountId;
    private UUID languageId;
    private Accounts testAccount;
    private Language japanese;
    private UserLanguage userLanguage;

    @BeforeEach
    void setUp() {
        userLanguageService = new UserLanguageService(
            messageSource,
            userLanguageRepository,
            accountsRepository,
            languageRepository,
            userLanguageBuilder,
            userProfileRepository,
            userProfileBuilder
        );

        accountId = UUID.randomUUID();
        languageId = UUID.randomUUID();

        testAccount = Accounts.builder()
                .id(accountId)
                .username("testuser")
                .email("test@example.com")
                .build();

        japanese = Language.builder()
                .id(languageId)
                .code("ja")
                .name("Japanese")
                .isActive(true)
                .build();

        userLanguage = UserLanguage.builder()
                .id(UUID.randomUUID())
                .account(testAccount)
                .language(japanese)
                .languageType(LanguageType.LEARNING)
                .proficiencyLevel(ProficiencyLevel.A2)
                
                .startedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should add language successfully")
    void shouldAddLanguageSuccessfully() {
        AddUserLanguageRequest request = AddUserLanguageRequest.builder()
                .languageId(languageId)
                .languageType(LanguageType.LEARNING)
                .proficiencyLevel(ProficiencyLevel.A1)
                
                .build();

        UserLanguageResponse expectedResponse = UserLanguageResponse.builder()
                .id(userLanguage.getId())
                .languageId(languageId)
                .languageCode("ja")
                .languageName("Japanese")
                .languageType(LanguageType.LEARNING)
                .proficiencyLevel(ProficiencyLevel.A2)
                
                .startedAt(userLanguage.getStartedAt())
                .build();

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(japanese));
        when(userLanguageRepository.existsByAccount_IdAndLanguage_Id(accountId, languageId)).thenReturn(false);
        when(userLanguageBuilder.buildUserLanguage(any(Accounts.class), any(Language.class), any(AddUserLanguageRequest.class)))
                .thenReturn(userLanguage);
        when(userLanguageRepository.save(any(UserLanguage.class))).thenReturn(userLanguage);
        when(userLanguageBuilder.mapToUserLanguageResponse(any(UserLanguage.class)))
                .thenReturn(expectedResponse);

        UserLanguageResponse result = userLanguageService.addLanguage(accountId, request);

        assertThat(result).isNotNull();
        assertThat(result.getLanguageCode()).isEqualTo("ja");
        assertThat(result.getLanguageType()).isEqualTo(LanguageType.LEARNING);
        verify(userLanguageRepository).save(any(UserLanguage.class));
    }

    @Test
    @DisplayName("Should throw exception when language already added")
    void shouldThrowExceptionWhenLanguageAlreadyAdded() {
        
        AddUserLanguageRequest request = AddUserLanguageRequest.builder()
                .languageId(languageId)
                .languageType(LanguageType.LEARNING)
                .proficiencyLevel(ProficiencyLevel.A1)
                .build();

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(japanese));
        when(userLanguageRepository.existsByAccount_IdAndLanguage_Id(accountId, languageId)).thenReturn(true);
        
        assertThatThrownBy(() -> userLanguageService.addLanguage(accountId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Language already added"); // Message en français depuis messages_fr.properties

        verify(userLanguageRepository, never()).save(any(UserLanguage.class));
    }

    @Test
    @DisplayName("Should update language successfully")
    void shouldUpdateLanguageSuccessfully() {

        UpdateUserLanguageRequest request = UpdateUserLanguageRequest.builder()
                .proficiencyLevel(ProficiencyLevel.B1)
                .build();

        UserLanguageResponse expectedResponse = UserLanguageResponse.builder()
                .id(userLanguage.getId())
                .languageId(languageId)
                .languageCode("ja")
                .languageName("Japanese")
                .languageType(LanguageType.LEARNING)
                .proficiencyLevel(ProficiencyLevel.B1)
                .startedAt(userLanguage.getStartedAt())
                .build();

        when(userLanguageRepository.findByAccount_IdAndLanguage_Id(accountId, languageId))
                .thenReturn(Optional.of(userLanguage));
        when(userLanguageRepository.save(any(UserLanguage.class))).thenReturn(userLanguage);
        when(userLanguageBuilder.mapToUserLanguageResponse(any(UserLanguage.class)))
                .thenReturn(expectedResponse);

        UserLanguageResponse result = userLanguageService.updateLanguage(accountId, languageId, request);

        assertThat(result).isNotNull();
        assertThat(result.getProficiencyLevel()).isEqualTo(ProficiencyLevel.B1);
        verify(userLanguageRepository).save(any(UserLanguage.class));
    }

    @Test
    @DisplayName("Should remove language successfully")
    void shouldRemoveLanguageSuccessfully() {
        
        when(userLanguageRepository.existsByAccount_IdAndLanguage_Id(accountId, languageId)).thenReturn(true);
        doNothing().when(userLanguageRepository).deleteByAccount_IdAndLanguage_Id(accountId, languageId);
        
        userLanguageService.removeLanguage(accountId, languageId);
        
        verify(userLanguageRepository).deleteByAccount_IdAndLanguage_Id(accountId, languageId);
    }

    @Test
    @DisplayName("Should get user languages by type")
    void shouldGetUserLanguagesByType() {
        UserLanguageResponse expectedResponse = UserLanguageResponse.builder()
                .id(userLanguage.getId())
                .languageId(languageId)
                .languageCode("ja")
                .languageName("Japanese")
                .languageType(LanguageType.LEARNING)
                .proficiencyLevel(ProficiencyLevel.A2)
                .startedAt(userLanguage.getStartedAt())
                .build();

        when(userLanguageRepository.findByAccount_IdAndLanguageType(accountId, LanguageType.LEARNING))
                .thenReturn(Arrays.asList(userLanguage));
        when(userLanguageBuilder.mapToUserLanguageResponse(any(UserLanguage.class)))
                .thenReturn(expectedResponse);

        List<UserLanguageResponse> result = userLanguageService.getUserLanguagesByType(accountId, LanguageType.LEARNING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLanguageType()).isEqualTo(LanguageType.LEARNING);
    }
}
