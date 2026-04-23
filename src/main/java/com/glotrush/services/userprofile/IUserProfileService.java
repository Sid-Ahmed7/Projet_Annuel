package com.glotrush.services.userprofile;

import java.util.UUID;

import com.glotrush.dto.request.NotificationPreferencesRequest;
import com.glotrush.dto.request.PasswordRequest;
import com.glotrush.dto.request.UpdateProfileRequest;
import com.glotrush.dto.response.NotificationPreferencesResponse;
import com.glotrush.dto.response.StreakResponse;
import com.glotrush.dto.response.UserProfileResponse;

public interface IUserProfileService {
    
    UserProfileResponse getProfile(UUID accountId);

    UserProfileResponse updateProfile(UUID accountId, UpdateProfileRequest request);
    
    UserProfileResponse getPublicProfile(UUID accountId);

    void changePassword(UUID accountId, PasswordRequest request);

    UserProfileResponse addActiveLanguage(UUID accountId, UUID languageId);
    
    StreakResponse getStreak(UUID accountId);

    NotificationPreferencesResponse getNotificationPreferences(UUID accountId);

    NotificationPreferencesResponse updateNotificationPreferences(UUID accountId, NotificationPreferencesRequest request);
}
