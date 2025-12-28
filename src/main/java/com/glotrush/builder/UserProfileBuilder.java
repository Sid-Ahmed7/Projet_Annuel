package com.glotrush.builder;

import java.util.List;

import org.springframework.stereotype.Component;

import com.glotrush.entities.Accounts;
import com.glotrush.entities.UserProfile;
import com.glotrush.entities.UserLanguage;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.dto.response.UserProfileResponse;
import com.glotrush.dto.response.UserLanguageResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserProfileBuilder {

    private final UserProfileRepository userProfileRepository;

    public UserProfile createDefaultProfile(Accounts account) {
        UserProfile profile = UserProfile.builder()
                .account(account)
                .displayName(account.getUsername())
                .isPublic(true)
                .build();
        return userProfileRepository.save(profile);
    }


    public UserProfileResponse mapToUserProfileResponse(Accounts account, UserProfile profile, List<UserLanguageResponse> languages) {
        return UserProfileResponse.builder()
                .id(profile.getId())
                .accountId(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .photoUrl(profile.getPhotoUrl())
                .bio(profile.getBio())
                .displayName(profile.getDisplayName())
                .countryCode(profile.getCountryCode())
                .timezone(profile.getTimezone())
                .isPublic(profile.getIsPublic())
                .languages(languages)
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
                .isPrimary(userLanguage.getIsPrimary())
                .startedAt(userLanguage.getStartedAt())
                .build();
    }
}

