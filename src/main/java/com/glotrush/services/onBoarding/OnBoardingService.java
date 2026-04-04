package com.glotrush.services.onBoarding;

import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.glotrush.builder.UserProfileBuilder;
import com.glotrush.dto.request.AddUserLanguageRequest;
import com.glotrush.dto.request.FirstLanguageRequest;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.UserProfile;
import com.glotrush.enumerations.LanguageType;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.services.languages.IUserLanguageService;
import com.glotrush.services.userprofile.IUserProfileService;
import com.glotrush.services.userprofile.UserProfileService;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OnBoardingService implements IOnBoardingService {

    private final AccountsRepository accountsRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileBuilder userProfileBuilder;
    private final IUserLanguageService userLanguageService;
    private final IUserProfileService userProfileService;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public void completeOnboarding(UUID accountId, FirstLanguageRequest request) {
    
        Accounts account = accountsRepository.findById(accountId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.user_not_found",null, LocaleUtils.getCurrentLocale())));

        if(request.getLanguageId() != null) {
            AddUserLanguageRequest addLanguageRequest = new AddUserLanguageRequest().builder()
                .languageId(request.getLanguageId())
                .languageType(LanguageType.LEARNING)
                .build();
                userLanguageService.addLanguage(accountId, addLanguageRequest);
                userProfileService.addActiveLanguage(accountId, request.getLanguageId());
        }

        UserProfile profile = userProfileRepository.findByAccount_Id(accountId).orElseGet(() -> userProfileBuilder.createDefaultProfile(account, false));
        profile.setHasCompletedOnboarding(true);
        userProfileRepository.save(profile);    
    }
}
