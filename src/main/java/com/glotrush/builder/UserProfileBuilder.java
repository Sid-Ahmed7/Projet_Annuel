package com.glotrush.builder;

import java.util.List;

import org.springframework.stereotype.Component;

import com.glotrush.entities.Accounts;
import com.glotrush.entities.Language;
import com.glotrush.entities.UserProfile;
import com.glotrush.entities.UserLanguage;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.dto.response.UserProfileResponse;
import com.glotrush.dto.response.LanguageResponse;
import com.glotrush.dto.response.UserLanguageResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserProfileBuilder {

    private final UserProfileRepository userProfileRepository;

    public UserProfile createDefaultProfile(Accounts account, boolean hasCompletedOnboarding) {
        UserProfile profile = UserProfile.builder()
                .account(account)
                .isPublic(true)
                .hasCompletedOnboarding(hasCompletedOnboarding)
                .build();
        return userProfileRepository.save(profile);
    }


    public UserProfileResponse mapToUserProfileResponse(Accounts account, UserProfile profile, List<UserLanguageResponse> languages) {
       LanguageResponse activeLanguage = null;
        if (profile.getActiveLanguage() != null) {
            Language activeLang = profile.getActiveLanguage();
            activeLanguage = LanguageResponse.builder()
                    .id(activeLang.getId())
                    .code(activeLang.getCode())
                    .name(activeLang.getName())
                    .isActive(activeLang.getIsActive())
                    .build();
        }
        return UserProfileResponse.builder()
                .id(profile.getId())
                .accountId(account.getId())
                .activeLanguage(activeLanguage)
                .username(account.getUsername())
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .photoUrl(profile.getPhotoUrl())
                .bio(profile.getBio())
                .countryCode(profile.getCountryCode())
                .timezone(profile.getTimezone())
                .isPublic(profile.getIsPublic())
                .languages(languages)
                .hasCompletedOnboarding(profile.getHasCompletedOnboarding())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

  
    public UserLanguageResponse mapToUserLanguageResponse(UserLanguage userLanguage) {
        return UserLanguageResponse.builder()
                .id(userLanguage.getId())
                .languageId(userLanguage.getLanguage().getId())
                .languageCode(userLanguage.getLanguage().getCode())
                .languageName(userLanguage.getLanguage().getName())
                .languageType(userLanguage.getLanguageType())
                .proficiencyLevel(userLanguage.getProficiencyLevel())
                .startedAt(userLanguage.getStartedAt())
                .build();
    }
}

