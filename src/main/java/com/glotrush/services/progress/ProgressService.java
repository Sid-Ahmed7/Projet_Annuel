package com.glotrush.services.progress;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.glotrush.builder.ProgressBuilder;
import com.glotrush.dto.response.ProgressOverviewResponse;
import com.glotrush.dto.response.UserProgressResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Topic;
import com.glotrush.entities.UserProgress;
import com.glotrush.exceptions.ResourceNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.UserProgressRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProgressService implements IProgressService {

    private final UserProgressRepository userProgressRepository;
    private final TopicRepository topicRepository;
    private final AccountsRepository accountsRepository;
    private final ProgressBuilder progressBuilder;

    private static final int BASE_XP_PER_LEVEL = 1000;

    @Override
    public ProgressOverviewResponse getProgressOverview(UUID accountId) {
        List<UserProgress> allProgress = userProgressRepository.findByAccount_Id(accountId);

        Long totalXP = allProgress.stream().mapToLong(UserProgress::getTotalXP).sum();
        Integer overallLevel = calculateOverallLevel(totalXP);
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
                .orElseThrow(() -> new ResourceNotFoundException("Progress not found for this topic"));
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
                            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
                    Topic topic = topicRepository.findById(topicId)
                            .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

                    UserProgress newProgress = UserProgress.builder()
                            .account(account)
                            .topic(topic)
                            .totalXP(0L)
                            .level(1)
                            .currentLevelXP(0L)
                            .nextLevelXP(100L)
                            .completedLessons(0)
                            .completionPercentage(0.0)
                            .correctAnswers(0)
                            .totalAnswers(0)
                            .accuracy(0.0)
                            .studyStreak(0)
                            .build();

                    return userProgressRepository.save(newProgress);
                });
    }

    @Override
    public UserProgress addXP(UUID accountId, UUID topicId, Integer xpToAdd) {
        UserProgress progress = getOrCreateProgress(accountId, topicId);

        progress.setTotalXP(progress.getTotalXP() + xpToAdd);
        progress.setCurrentLevelXP(progress.getCurrentLevelXP() + xpToAdd);

        while (progress.getCurrentLevelXP() >= progress.getNextLevelXP()) {
            progress.setCurrentLevelXP(progress.getCurrentLevelXP() - progress.getNextLevelXP());
            progress.setLevel(progress.getLevel() + 1);
            progress.setNextLevelXP(calculateNextLevelXP(progress.getLevel()));
        }

        return userProgressRepository.save(progress);
    }

    @Override
    public UserProgress incrementLessonCompletion(UUID accountId, UUID topicId) {
        UserProgress progress = getOrCreateProgress(accountId, topicId);
        Topic topic = progress.getTopic();

        progress.setCompletedLessons(progress.getCompletedLessons() + 1);
        progress.calculateCompletionPercentage(topic.getTotalLessons());

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

    private Integer calculateOverallLevel(Long totalXP) {
        return (int) (totalXP / BASE_XP_PER_LEVEL) + 1;
    }

    private Long calculateNextLevelXP(Integer level) {
        return 100L + (level - 1) * 50L;
    }
}
