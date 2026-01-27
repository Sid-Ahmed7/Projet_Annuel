package com.glotrush.services.userprofile;

import java.util.Locale;
import java.util.UUID;
import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.glotrush.builder.UserProfileBuilder;
import com.glotrush.dto.request.UpdateProfileRequest;
import com.glotrush.dto.response.UserLanguageResponse;
import com.glotrush.dto.response.UserProfileResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.UserProfile;
import com.glotrush.exceptions.ProfilePrivateException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.UserLanguageRepository;
import com.glotrush.repositories.UserProfileRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileService implements IUserProfileService {

    private final MessageSource messageSource;
    private final UserProfileRepository userProfileRepository;
    private final AccountsRepository accountsRepository;
    private final UserLanguageRepository userLanguageRepository;
    private final UserProfileBuilder userProfileBuilder;

    protected final Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }
    @Override
    @Transactional
    public UserProfileResponse getProfile(UUID accountId) {
        Accounts account = accountsRepository.findById(accountId)
            .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.user.not_found", null, getCurrentLocale())));

        UserProfile profile = userProfileRepository.findByAccount_Id(accountId)
            .orElseGet(() -> userProfileBuilder.createDefaultProfile(account));

        List<UserLanguageResponse> languages = userLanguageRepository.findByAccount_Id(accountId)
            .stream()
            .map(userProfileBuilder::mapToUserLanguageResponse)
            .toList();

        return userProfileBuilder.mapToUserProfileResponse(account, profile, languages);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UUID accountId, UpdateProfileRequest request) {
        Accounts account = accountsRepository.findById(accountId)
            .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.user.not_found", null, getCurrentLocale())));

        UserProfile profile = userProfileRepository.findByAccount_Id(accountId)
            .orElseGet(() -> userProfileBuilder.createDefaultProfile(account));

        if (request.getPhotoUrl() != null) {
            profile.setPhotoUrl(request.getPhotoUrl());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName());
        }
        if (request.getCountryCode() != null) {
            profile.setCountryCode(request.getCountryCode().toUpperCase());
        }
        if (request.getTimezone() != null) {
            profile.setTimezone(request.getTimezone());
        }
        if (request.getIsPublic() != null) {
            profile.setIsPublic(request.getIsPublic());
        }

        userProfileRepository.save(profile);

         List<UserLanguageResponse> languages = userLanguageRepository.findByAccount_Id(accountId)
            .stream()
            .map(userProfileBuilder::mapToUserLanguageResponse)
            .toList();

        return userProfileBuilder.mapToUserProfileResponse(account, profile, languages);


    }

    @Override
    public UserProfileResponse getPublicProfile(UUID accountId) {
        Accounts account = accountsRepository.findById(accountId)
            .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.user.not_found", null, getCurrentLocale())));

        UserProfile profile = userProfileRepository.findByAccount_Id(accountId)
            .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.profile.not_found", null, getCurrentLocale())));
  
        if(!profile.getIsPublic()) {
            throw new ProfilePrivateException(messageSource.getMessage("error.profile.private", null, getCurrentLocale()));
        }
        List<UserLanguageResponse> languages = userLanguageRepository.findByAccount_Id(accountId)
            .stream()
            .map(userProfileBuilder::mapToUserLanguageResponse)
            .toList();

        return userProfileBuilder.mapToUserProfileResponse(account, profile, languages);
    }
}

