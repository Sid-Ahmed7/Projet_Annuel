package com.glotrush.services.onBoarding;

import java.util.UUID;

import com.glotrush.dto.request.FirstLanguageRequest;

public interface  IOnBoardingService {
    void completeOnboarding(UUID accountId, FirstLanguageRequest request);
    
}
