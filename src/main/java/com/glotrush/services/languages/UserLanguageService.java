package com.glotrush.services.languages;

    import java.util.List;
    import java.util.UUID;

    import org.springframework.context.MessageSource;
    import org.springframework.stereotype.Service;

    import com.glotrush.builder.UserLanguageBuilder;
    import com.glotrush.builder.UserProfileBuilder;
    import com.glotrush.dto.request.AddUserLanguageRequest;
    import com.glotrush.dto.request.UpdateUserLanguageRequest;
    import com.glotrush.dto.response.UserLanguageResponse;
    import com.glotrush.entities.Accounts;
    import com.glotrush.entities.Language;
    import com.glotrush.entities.UserLanguage;
    import com.glotrush.entities.UserProfile;
    import com.glotrush.enumerations.LanguageType;
    import com.glotrush.exceptions.LanguageException;
    import com.glotrush.exceptions.UserLanguageException;
    import com.glotrush.exceptions.UserNotFoundException;
    import com.glotrush.repositories.AccountsRepository;
    import com.glotrush.repositories.LanguageRepository;
    import com.glotrush.repositories.UserLanguageRepository;
    import com.glotrush.repositories.UserProfileRepository;
    import com.glotrush.utils.LocaleUtils;

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
        private final UserProfileRepository userProfileRepository;
        private final UserProfileBuilder userProfileBuilder;


        @Override
        @Transactional
        public UserLanguageResponse addLanguage(UUID accountId, AddUserLanguageRequest request) {
            Accounts account = accountsRepository.findById(accountId)
                    .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.user.not_found", null, LocaleUtils.getCurrentLocale())));

            Language language = languageRepository.findById(request.getLanguageId())
                    .orElseThrow(() -> new LanguageException(messageSource.getMessage("error.language.not_found", null, LocaleUtils.getCurrentLocale())));

            if (userLanguageRepository.existsByAccount_IdAndLanguage_Id(accountId, request.getLanguageId())) {
                throw new LanguageException(messageSource.getMessage("error.language.already_added", null, LocaleUtils.getCurrentLocale()));
            }

            if (request.getLanguageType() == LanguageType.NATIVE
                    && userLanguageRepository.existsByAccount_IdAndLanguageType(accountId, LanguageType.NATIVE)) {
                throw new LanguageException(messageSource.getMessage("error.language.already_added", null, LocaleUtils.getCurrentLocale()));
            }

            UserLanguage userLanguage = userLanguageBuilder.buildUserLanguage(account, language, request);

            userLanguage = userLanguageRepository.save(userLanguage);

            if(request.getLanguageType() == LanguageType.LEARNING) {
                Long countLanguages = userLanguageRepository.countByAccount_IdAndLanguageType(accountId, LanguageType.LEARNING);
                if(countLanguages == 1) {
                    UserProfile profile = userProfileRepository.findByAccount_Id(accountId).orElseGet(() -> userProfileBuilder.createDefaultProfile(account, true));
                    profile.setActiveLanguage(userLanguage.getLanguage());
                    userProfileRepository.save(profile);
                }   
            }

            return userLanguageBuilder.mapToUserLanguageResponse(userLanguage);
        }

        @Override
        @Transactional
        public UserLanguageResponse updateLanguage(UUID accountId, UUID languageId, UpdateUserLanguageRequest request) {
            UserLanguage userLanguage = userLanguageRepository.findByAccount_IdAndLanguage_Id(accountId, languageId)
                    .orElseThrow(() -> new UserLanguageException(messageSource.getMessage("error.language.user_language_not_found", null, LocaleUtils.getCurrentLocale())));

        if (request.getLanguageType() != null && !request.getLanguageType().equals(userLanguage.getLanguageType())) {
            throw new UserLanguageException(messageSource.getMessage("error.language.cannot_change_type", null, LocaleUtils.getCurrentLocale()));
        }
            if (request.getProficiencyLevel() != null) {
                userLanguage.setProficiencyLevel(request.getProficiencyLevel());
            }

            userLanguage = userLanguageRepository.save(userLanguage);

            return userLanguageBuilder.mapToUserLanguageResponse(userLanguage);
        }

        @Override
        @Transactional
        public void removeLanguage(UUID accountId, UUID languageId) {
            UserLanguage userLanguage = userLanguageRepository.findByIdAndAccount_Id(languageId, accountId)
                    .orElseThrow(() -> new UserLanguageException(messageSource.getMessage("error.language.user_language_not_found", null, LocaleUtils.getCurrentLocale())));

            UserProfile profile = userProfileRepository.findByAccount_Id(accountId).orElse(null);
            if(profile != null && profile.getActiveLanguage() != null && profile.getActiveLanguage().getId().equals(userLanguage.getLanguage().getId())) {
                userLanguageRepository.findByAccount_IdAndLanguageType(accountId, LanguageType.LEARNING)
                        .stream()
                        .filter(ul -> !ul.getLanguage().getId().equals(userLanguage.getLanguage().getId()))
                        .findFirst()
                        .ifPresentOrElse(language -> profile.setActiveLanguage(language.getLanguage()),
                                () -> profile.setActiveLanguage(null)
                        );
                userProfileRepository.save(profile);
            }
            userLanguageRepository.delete(userLanguage);
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
