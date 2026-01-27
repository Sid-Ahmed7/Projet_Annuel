package com.glotrush.services.languages;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.glotrush.builder.UserLanguageBuilder;
import com.glotrush.dto.request.AddUserLanguageRequest;
import com.glotrush.dto.request.UpdateUserLanguageRequest;
import com.glotrush.dto.response.UserLanguageResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Language;
import com.glotrush.entities.UserLanguage;
import com.glotrush.enumerations.LanguageType;
import com.glotrush.exceptions.LanguageException;
import com.glotrush.exceptions.UserLanguageException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.LanguageRepository;
import com.glotrush.repositories.UserLanguageRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserLanguageService implements IUserLanguageService {
    private final MessageSource messageSource;
    private final UserLanguageRepository userLanguageRepository;
    private final AccountsRepository accountsRepository;
    private final LanguageRepository languageRepository;
    private final UserLanguageBuilder userLanguageBuilder;

    protected final Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    @Override
    @Transactional
    public UserLanguageResponse addLanguage(UUID accountId, AddUserLanguageRequest request) {
        Accounts account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.user.not_found", null, getCurrentLocale())));

        Language language = languageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new LanguageException(messageSource.getMessage("error.language.not_found", null, getCurrentLocale())));

        if (userLanguageRepository.existsByAccount_IdAndLanguage_Id(accountId, request.getLanguageId())) {
            throw new LanguageException(messageSource.getMessage("error.language.already_added", null, getCurrentLocale()));
        }

        UserLanguage userLanguage = userLanguageBuilder.buildUserLanguage(account, language, request);

        userLanguage = userLanguageRepository.save(userLanguage);

        return userLanguageBuilder.mapToUserLanguageResponse(userLanguage);
    }

    @Override
    @Transactional
    public UserLanguageResponse updateLanguage(UUID accountId, UUID languageId, UpdateUserLanguageRequest request) {
        UserLanguage userLanguage = userLanguageRepository.findByAccount_IdAndLanguage_Id(accountId, languageId)
                .orElseThrow(() -> new UserLanguageException(messageSource.getMessage("error.language.user_language_not_found", null, getCurrentLocale())));

        if (request.getLanguageType() != null) {
            userLanguage.setLanguageType(request.getLanguageType());
        }
        if (request.getProficiencyLevel() != null) {
            userLanguage.setProficiencyLevel(request.getProficiencyLevel());
        }
        if (request.getIsPrimary() != null) {
            userLanguage.setIsPrimary(request.getIsPrimary());
        }

        userLanguage = userLanguageRepository.save(userLanguage);

        return userLanguageBuilder.mapToUserLanguageResponse(userLanguage);
    }

    @Override
    @Transactional
    public void removeLanguage(UUID accountId, UUID languageId) {
        if (!userLanguageRepository.existsByAccount_IdAndLanguage_Id(accountId, languageId)) {
            throw new UserLanguageException(messageSource.getMessage("error.language.user_language_not_found", null, getCurrentLocale()));
        }
        userLanguageRepository.deleteByAccount_IdAndLanguage_Id(accountId, languageId);
    }

    @Override
    @Transactional()
    public List<UserLanguageResponse> getUserLanguages(UUID accountId) {
        return userLanguageRepository.findByAccount_Id(accountId)
                .stream()
                .map(userLanguageBuilder::mapToUserLanguageResponse)
                .toList();
    }

    @Override
    @Transactional()
    public List<UserLanguageResponse> getUserLanguagesByType(UUID accountId, LanguageType type) {
        return userLanguageRepository.findByAccount_IdAndLanguageType(accountId, type)
                .stream()
                .map(userLanguageBuilder::mapToUserLanguageResponse)
                .toList();
    }
}
