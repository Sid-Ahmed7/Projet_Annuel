package com.glotrush.services.progress;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.glotrush.builder.ProgressBuilder;
import com.glotrush.dto.response.ProgressOverviewResponse;
import com.glotrush.dto.response.UserProgressResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Topic;
import com.glotrush.entities.UserProgress;
import com.glotrush.exceptions.ResourceNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.UserProgressRepository;

import com.glotrush.utils.LevelUtils;
import com.glotrush.utils.LocaleUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProgressService implements IProgressService {

    private final MessageSource messageSource;
    private final UserProgressRepository userProgressRepository;
    private final TopicRepository topicRepository;
    private final AccountsRepository accountsRepository;
    private final ProgressBuilder progressBuilder;
    private final LessonRepository lessonRepository;

    private static final int BASE_XP_PER_LEVEL = 1000;

    @Override
    public ProgressOverviewResponse getProgressOverview(UUID accountId) {
        List<UserProgress> allProgress = userProgressRepository.findByAccount_Id(accountId);

        Long totalXP = allProgress.stream().mapToLong(UserProgress::getTotalXP).sum();
        Integer overallLevel = LevelUtils.calculateLevel(totalXP);
        Integer totalTopicsStarted = allProgress.size();

        Integer totalLessonsCompleted = allProgress.stream().mapToInt(UserProgress::getCompletedLessons).sum();

        Double overallAccuracy = allProgress.stream()
                .filter(p -> p.getTotalAnswers() > 0)
                .mapToDouble(UserProgress::getAccuracy)
                .average()
                .orElse(0.0);

        Integer currentStreak = allProgress.stream()
                .mapToInt(UserProgress::getStudyStreak)
                .max()
                .orElse(0);

        List<UserProgressResponse> progressByTopic = allProgress.stream()
                .map(progressBuilder::mapToUserProgressResponse)
                .toList();

        return progressBuilder.buildProgressOverview(
                totalXP,
                overallLevel,
                totalTopicsStarted,
                totalLessonsCompleted,
                overallAccuracy,
                currentStreak,
                progressByTopic
        );
    }

    @Override
    public UserProgressResponse getProgressByTopic(UUID accountId, UUID topicId) {
        UserProgress progress = userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.progress.notfound", null, LocaleUtils.getCurrentLocale())));
        return progressBuilder.mapToUserProgressResponse(progress);
    }

    @Override
    public List<UserProgressResponse> getProgressByLanguage(UUID accountId, UUID languageId) {
        List<UserProgress> progressList = userProgressRepository.findByAccount_IdAndTopic_Language_Id(accountId, languageId);
        return progressList.stream()
                .map(progressBuilder::mapToUserProgressResponse)
                .toList();
    }

    @Override
    public UserProgress getOrCreateProgress(UUID accountId, UUID topicId) {
        return userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId)
                .orElseGet(() -> {
                    Accounts account = accountsRepository.findById(accountId)
                            .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, LocaleUtils.getCurrentLocale())));
                    Topic topic = topicRepository.findById(topicId)
                            .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale())));

                    UserProgress newProgress = UserProgress.builder()
                            .account(account)
                            .topic(topic)
                            .totalXP(0L)
                            .completedLessons(0)
                            .completionPercentage(0.0)
                            .correctAnswers(0)
                            .totalAnswers(0)
                            .accuracy(0.0)
                            .studyStreak(0)
                            .examPassed(false)
                            .build();

                    return userProgressRepository.save(newProgress);
                });
    }

    @Override
    public UserProgress addXP(UUID accountId, UUID topicId, Integer xpToAdd) {
        UserProgress progress = getOrCreateProgress(accountId, topicId);

        progress.setTotalXP(progress.getTotalXP() + xpToAdd);

        return userProgressRepository.save(progress);
    }

    @Override
    public UserProgress incrementLessonCompletion(UUID accountId, UUID topicId) {
        UserProgress progress = getOrCreateProgress(accountId, topicId);

        progress.setCompletedLessons(progress.getCompletedLessons() + 1);
        Integer totalLessons = lessonRepository.countByTopic_Id(topicId);
        progress.calculateCompletionPercentage(totalLessons);

        return userProgressRepository.save(progress);
    }

    @Override
    public UserProgress updateAnswerStats(UUID accountId, UUID topicId, Integer correct, Integer total) {
        UserProgress progress = getOrCreateProgress(accountId, topicId);

        progress.setCorrectAnswers(progress.getCorrectAnswers() + correct);
        progress.setTotalAnswers(progress.getTotalAnswers() + total);
        progress.calculateAccuracy();

        return userProgressRepository.save(progress);
    }

    @Override
    public UserProgress updateLastStudiedAt(UUID accountId, UUID topicId) {
        UserProgress progress = getOrCreateProgress(accountId, topicId);
        progress.setLastStudiedAt(LocalDateTime.now());
        return userProgressRepository.save(progress);
    }
}
