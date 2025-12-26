package com.glotrush.builder;

import com.glotrush.dto.request.AddUserLanguageRequest;
import com.glotrush.dto.response.UserLanguageResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Language;
import com.glotrush.entities.UserLanguage;

public class UserLanguageBuilder {
    
    public UserLanguage buildUserLanguage(Accounts account,Language language,AddUserLanguageRequest request) {
        return UserLanguage.builder()
                .account(account)
                .language(language)
                .languageType(request.getLanguageType())
                .proficiencyLevel(request.getProficiencyLevel())
                .isPrimary(request.getIsPrimary())
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
