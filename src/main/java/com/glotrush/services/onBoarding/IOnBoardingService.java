package com.glotrush.services.onBoarding;

import java.util.UUID;

import com.glotrush.dto.request.OnBoardingRequest;
import com.glotrush.dto.response.UserProfileResponse;

public interface  IOnBoardingService {
    UserProfileResponse completeOnboarding(UUID accountId, OnBoardingRequest request);
    
}
