package com.glotrush.services.languages;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.request.AddUserLanguageRequest;
import com.glotrush.dto.request.UpdateUserLanguageRequest;
import com.glotrush.dto.response.UserLanguageResponse;
import com.glotrush.enumerations.LanguageType;

public interface IUserLanguageService {
     UserLanguageResponse addLanguage(UUID accountId, AddUserLanguageRequest request);
     
     UserLanguageResponse updateLanguage(UUID accountId, UUID languageId, UpdateUserLanguageRequest request);

    void removeLanguage(UUID accountId, UUID languageId);

    List<UserLanguageResponse> getUserLanguages(UUID accountId);

    List<UserLanguageResponse> getUserLanguagesByType(UUID accountId, LanguageType type);

    



}
