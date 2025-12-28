package com.glotrush.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.glotrush.builder.UserProfileBuilder;
import com.glotrush.dto.request.UpdateProfileRequest;
import com.glotrush.dto.response.UserProfileResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.UserProfile;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.UserLanguageRepository;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.services.userprofile.UserProfileService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService Unit Tests")
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private UserLanguageRepository userLanguageRepository;

    @Mock
    private UserProfileBuilder userProfileBuilder;

    @InjectMocks
    private UserProfileService profileService;

    private UUID accountId;
    private Accounts testAccount;
    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();

        testAccount = Accounts.builder()
                .id(accountId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        testProfile = UserProfile.builder()
                .id(UUID.randomUUID())
                .account(testAccount)
                .displayName("Test User")
                .bio("Test bio")
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should retrieve existing profile successfully")
    void shouldGetProfileSuccessfully() {
        UserProfileResponse expectedResponse = UserProfileResponse.builder()
                .username("testuser")
                .email("test@example.com")
                .bio("Test bio")
                .build();

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(userProfileRepository.findByAccount_Id(accountId)).thenReturn(Optional.of(testProfile));
        when(userLanguageRepository.findByAccount_Id(accountId)).thenReturn(new ArrayList<>());
        when(userProfileBuilder.mapToUserProfileResponse(any(), any(), any())).thenReturn(expectedResponse);

        UserProfileResponse result = profileService.getProfile(accountId);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getBio()).isEqualTo("Test bio");

        verify(accountsRepository).findById(accountId);
        verify(userProfileRepository).findByAccount_Id(accountId);
        verify(userLanguageRepository).findByAccount_Id(accountId);
    }

    @Test
    @DisplayName("Should create default profile if not exists")
    void shouldCreateDefaultProfileWhenNotExists() {
        UserProfileResponse expectedResponse = UserProfileResponse.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(userProfileRepository.findByAccount_Id(accountId)).thenReturn(Optional.empty());
        when(userProfileBuilder.createDefaultProfile(any(Accounts.class))).thenReturn(testProfile);
        when(userLanguageRepository.findByAccount_Id(accountId)).thenReturn(new ArrayList<>());
        when(userProfileBuilder.mapToUserProfileResponse(any(), any(), any())).thenReturn(expectedResponse);

        UserProfileResponse result = profileService.getProfile(accountId);

        assertThat(result).isNotNull();
        verify(userProfileBuilder).createDefaultProfile(any(Accounts.class));
    }

    @Test
    @DisplayName("Should throw exception when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        when(accountsRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile(accountId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(accountsRepository).findById(accountId);
        verifyNoInteractions(userProfileRepository);
    }

    @Test
    @DisplayName("Should update profile successfully")
    void shouldUpdateProfileSuccessfully() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .bio("Updated bio")
                .displayName("Updated Name")
                .countryCode("fr")
                .timezone("Europe/Paris")
                .isPublic(false)
                .build();

        UserProfileResponse expectedResponse = UserProfileResponse.builder()
                .bio("Updated bio")
                .displayName("Updated Name")
                .build();

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(userProfileRepository.findByAccount_Id(accountId)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);
        when(userLanguageRepository.findByAccount_Id(accountId)).thenReturn(new ArrayList<>());
        when(userProfileBuilder.mapToUserProfileResponse(any(), any(), any())).thenReturn(expectedResponse);

        UserProfileResponse result = profileService.updateProfile(accountId, request);

        assertThat(result).isNotNull();
        verify(userProfileRepository).save(any(UserProfile.class));
        assertThat(testProfile.getBio()).isEqualTo("Updated bio");
        assertThat(testProfile.getDisplayName()).isEqualTo("Updated Name");
        assertThat(testProfile.getCountryCode()).isEqualTo("FR");
    }

    @Test
    @DisplayName("Should get public profile when profile is public")
    void shouldGetPublicProfileWhenPublic() {
        UserProfileResponse expectedResponse = UserProfileResponse.builder()
                .isPublic(true)
                .build();

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(userProfileRepository.findByAccount_Id(accountId)).thenReturn(Optional.of(testProfile));
        when(userLanguageRepository.findByAccount_Id(accountId)).thenReturn(new ArrayList<>());
        when(userProfileBuilder.mapToUserProfileResponse(any(), any(), any())).thenReturn(expectedResponse);

        UserProfileResponse result = profileService.getPublicProfile(accountId);

        assertThat(result).isNotNull();
        assertThat(result.getIsPublic()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when profile is private")
    void shouldThrowExceptionWhenProfileIsPrivate() {
        testProfile.setIsPublic(false);
        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(userProfileRepository.findByAccount_Id(accountId)).thenReturn(Optional.of(testProfile));

        assertThatThrownBy(() -> profileService.getPublicProfile(accountId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Profile is private");
    }
}