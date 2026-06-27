package com.glotrush.services.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.glotrush.builder.AdminStatsBuilder;
import com.glotrush.constants.TimeConstants;
import com.glotrush.dto.response.stats.LearningStatsResponse;
import com.glotrush.dto.response.stats.TopicStatsResponse;
import com.glotrush.dto.response.stats.UserDetailsResponse;
import com.glotrush.dto.response.stats.UserEngagementResponse;
import com.glotrush.dto.response.stats.UserStatsListResponse;
import com.glotrush.dto.response.stats.UserStatsResponse;
import com.glotrush.dto.response.stats.UserEngagementResponse.UserGrowthByMonth;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.UserProfile;
import com.glotrush.entities.UserProgress;
import com.glotrush.enumerations.LessonSessionStatus;
import com.glotrush.enumerations.UserRole;
import com.glotrush.exceptions.ResourceNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.LessonSessionRepository;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.repositories.UserProgressRepository;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminStatsService implements IAdminStatsService {

    private final AccountsRepository accountsRepository;
    private final UserProgressRepository userProgressRepository;
    private final LessonSessionRepository lessonSessionRepository;
    private final UserProfileRepository userProfileRepository;
    private final MessageSource messageSource;
    private final AdminStatsBuilder adminStatsBuilder;

    @Override
    public UserEngagementResponse getUserEngagement() {
        LocalDateTime now = LocalDateTime.now();

        List<Object[]> rawMonthly = accountsRepository.countNewUsersByMonth(UserRole.USER, now.minusMonths(TimeConstants.TWELVE_HOURS));
        List<UserGrowthByMonth> newUsersByMonth = rawMonthly.stream().map(row -> UserGrowthByMonth.builder()
            .month((String) row[0])
            .count((Long) row[1])
            .build()).toList();

        return UserEngagementResponse.builder()
            .totalUsers(accountsRepository.countTotalUsers(UserRole.USER))
            .newUsersLastSevenDays(accountsRepository.countNewUsersSince(UserRole.USER, now.minusDays(TimeConstants.SEVEN_DAYS)))
            .activeUsersLastSevenDays(lessonSessionRepository.countActiveUsersSince(now.minusDays(TimeConstants.SEVEN_DAYS)))
            .usersWithActiveStreak(accountsRepository.countUsersWithActiveStreak(UserRole.USER))
            .newUsersByMonth(newUsersByMonth)
            .build();
    }

    @Override
    public LearningStatsResponse getLearningMetrics() {
        Long totalStudyTimeSeconds = lessonSessionRepository.sumTotalStudyTimeSeconds(LessonSessionStatus.COMPLETED);
        List<String> popularLanguages = userProgressRepository.findMostStudiedLanguages(PageRequest.of(0, 1));
        Double avgAccuracy = userProgressRepository.avgAccuracyGlobal();
        Double avgCompletion = userProgressRepository.avgCompletionPercentageGlobal();

        return LearningStatsResponse.builder()
            .avgAccuracy(avgAccuracy != null ? Math.round(avgAccuracy * 100 * 10.0) / 10.0 : 0.0)
            .avgCompletionPercentage(avgCompletion != null ? Math.round(avgCompletion * 10.0) / 10.0 : 0.0)
            .totalStudyTimeMinutes(totalStudyTimeSeconds != null ? totalStudyTimeSeconds / 60 : 0L)
            .mostPopularLanguage(popularLanguages.isEmpty() ? null : popularLanguages.get(0))
            .build();
    }

    @Override
    public UserStatsListResponse getUserStats(String search, Integer page, Integer size) {
        Page<Accounts> accounts = accountsRepository.findUsersWithSearch(UserRole.USER,(search != null && !search.isBlank()) ? search : null,PageRequest.of(page, size));

        List<UserStatsResponse> users = accounts.getContent().stream().map(account -> {
            UUID id = account.getId();
            UserProfile profile = userProfileRepository.findByAccount_Id(id).orElse(null);
            Long totalXP = userProgressRepository.getXpByAccountId(id);
            Long totalLessons = userProgressRepository.sumCompletedLessonsByAccountId(id);
            Double avgAccuracy = userProgressRepository.avgAccuracyByAccountId(id);
            Long studyTimeSeconds = lessonSessionRepository.sumStudyTimeByAccountId(id, LessonSessionStatus.COMPLETED);
            Long topicsCompleted = userProgressRepository.countCompletedTopicsByAccountId(id);
            LocalDateTime lastActivity = lessonSessionRepository.findLastActivityByAccountId(id, LessonSessionStatus.COMPLETED);
            return adminStatsBuilder.buildUserStatsResponse(account, profile, totalXP, totalLessons, avgAccuracy, studyTimeSeconds, topicsCompleted, lastActivity);
        }).toList();

        return UserStatsListResponse.builder()
            .users(users)
            .totalUsers(accounts.getTotalElements())
            .currentPage(page)
            .pageSize(size)
            .build();
    }

    @Override
    public UserDetailsResponse getUserDetail(UUID accountId) {
        Accounts account = accountsRepository.findById(accountId).orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, LocaleUtils.getCurrentLocale())));
        UserProfile profile = userProfileRepository.findByAccount_Id(accountId).orElse(null);

        Long totalXP = userProgressRepository.getXpByAccountId(accountId);
        Long totalLessons = userProgressRepository.sumCompletedLessonsByAccountId(accountId);
        Double avgAccuracy = userProgressRepository.avgAccuracyByAccountId(accountId);
        Long studyTimeSeconds = lessonSessionRepository.sumStudyTimeByAccountId(accountId, LessonSessionStatus.COMPLETED);
        Long topicsCompleted = userProgressRepository.countCompletedTopicsByAccountId(accountId);

        List<UserProgress> progresses = userProgressRepository.findByAccount_Id(accountId);
        List<TopicStatsResponse> topicStats = progresses.stream().map(userProgress -> {
            Long studyTime = lessonSessionRepository.sumStudyTimeByAccountIdAndTopicId(accountId, userProgress.getTopic().getId(), LessonSessionStatus.COMPLETED);
            return adminStatsBuilder.buildTopicStatsResponse(userProgress, studyTime);
        }).toList();

        return adminStatsBuilder.buildUserDetailsResponse(account, profile, totalXP, totalLessons, avgAccuracy,studyTimeSeconds, topicsCompleted, topicStats);
    }
}