package com.glotrush.services.admin;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.glotrush.dto.response.ProgressOverviewResponse;
import com.glotrush.dto.response.UserStatisticsResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.UserProfile;
import com.glotrush.exceptions.ResourceNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.UserLessonProgressRepository;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.services.progress.IProgressService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminStatisticsService implements IAdminStatisticsService {

    private final MessageSource messageSource;
    private final AccountsRepository accountsRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;
    private final IProgressService progressService;

    protected final Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    @Override
    public List<UserStatisticsResponse> getAllUsersStatistics() {
        List<Accounts> accounts = accountsRepository.findAll();
        return accounts.stream()
                .map(this::buildUserStatisticsResponse)
                .toList();
    }

    @Override
    public UserStatisticsResponse getUserStatisticsById(UUID userId) {
        Accounts account = accountsRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("error.admin.user_not_found", null, getCurrentLocale())));
        return buildUserStatisticsResponse(account);
    }

    private UserStatisticsResponse buildUserStatisticsResponse(Accounts account) {
        UUID accountId = account.getId();

        ProgressOverviewResponse progressOverview = progressService.getProgressOverview(accountId);
        Long totalTimeSpentSeconds = userLessonProgressRepository.sumTimeSpentSecondsByAccount_Id(accountId);
        if (totalTimeSpentSeconds == null) {
            totalTimeSpentSeconds = 0L;
        }

        String displayName = resolveDisplayName(account);

        return UserStatisticsResponse.builder()
                .userId(account.getId())
                .email(account.getEmail())
                .username(account.getUsername())
                .displayName(displayName)
                .role(account.getRole().name())
                .totalXP(progressOverview.getTotalXP())
                .overallLevel(progressOverview.getOverallLevel())
                .totalLessonsCompleted(progressOverview.getTotalLessonsCompleted())
                .totalTopicsStarted(progressOverview.getTotalTopicsStarted())
                .totalTimeSpentSeconds(totalTimeSpentSeconds)
                .overallAccuracy(progressOverview.getOverallAccuracy())
                .currentStreak(progressOverview.getCurrentStreak())
                .progressByTopic(progressOverview.getProgressByTopic())
                .build();
    }

    private String resolveDisplayName(Accounts account) {
        return userProfileRepository.findByAccount_Id(account.getId())
                .map(UserProfile::getDisplayName)
                .filter(name -> name != null && !name.isBlank())
                .orElse(account.getFirstName() + " " + account.getLastName());
    }
}
