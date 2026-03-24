package com.glotrush.builder;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.glotrush.dto.request.AddUserLanguageRequest;
import com.glotrush.dto.response.UserLanguageResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Language;
import com.glotrush.entities.UserLanguage;

@Component
public class UserLanguageBuilder {

    public UserLanguage buildUserLanguage(Accounts account, Language language, AddUserLanguageRequest request) {
        LocalDateTime now = LocalDateTime.now();
        return UserLanguage.builder()
                .account(account)
                .language(language)
                .languageType(request.getLanguageType())
                .proficiencyLevel(request.getProficiencyLevel())
                .startedAt(now)
                .createdAt(now)
                .updatedAt(now)
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
