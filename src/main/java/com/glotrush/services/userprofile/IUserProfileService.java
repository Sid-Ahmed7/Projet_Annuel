package com.glotrush.services.userprofile;

import java.util.UUID;

import com.glotrush.dto.request.UpdateProfileRequest;
import com.glotrush.dto.response.UserProfileResponse;

public interface IUserProfileService {
    
    UserProfileResponse getProfile(UUID accountId);

    UserProfileResponse updateProfile(UUID accountId, UpdateProfileRequest request);
    
    UserProfileResponse getPublicProfile(UUID accountId);

    
}
