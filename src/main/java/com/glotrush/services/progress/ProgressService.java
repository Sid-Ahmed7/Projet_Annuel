package com.glotrush.services.progress;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.glotrush.builder.ProgressBuilder;
import com.glotrush.dto.response.LanguageLevelResponse;
import com.glotrush.dto.response.LastLessonResponse;
import com.glotrush.dto.response.ProgressOverviewResponse;
import com.glotrush.dto.response.UserProgressResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.Topic;
import com.glotrush.entities.UserLanguage;
import com.glotrush.entities.UserLessonProgress;
import com.glotrush.entities.UserProgress;
import com.glotrush.enumerations.LanguageType;
import com.glotrush.exceptions.ResourceNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.UserLanguageRepository;
import com.glotrush.repositories.UserLessonProgressRepository;
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
    private final UserLessonProgressRepository userLessonProgressRepository;
    private final UserLanguageRepository userLanguageRepository;



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

        List<LanguageLevelResponse> progressByLanguage = getLanguageLevel(accountId);

        long currentLevelXP = LevelUtils.calculateCurrentLevelXP(totalXP);
        long nextLevelXP = LevelUtils.calculateNextLevelXP((long) overallLevel);
        double levelProgressPercentage = LevelUtils.calculateLevelProgressPercentage(totalXP);

        return progressBuilder.buildProgressOverview(
                totalXP,
                overallLevel,
                totalTopicsStarted,
                totalLessonsCompleted,
                overallAccuracy,
                currentStreak,
                currentLevelXP,
                nextLevelXP,
                levelProgressPercentage,
                progressByTopic,
                progressByLanguage

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
                            .examAttempts(0)
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

    @Override
    public List<LanguageLevelResponse> getLanguageLevel(UUID accountId) {
        List<UserLanguage> userLanguages = userLanguageRepository
                .findByAccount_IdAndLanguageType(accountId, LanguageType.LEARNING);

        List<UserProgress> allProgress = userProgressRepository.findByAccount_Id(accountId);
        Map<UUID, Long> xpByLanguage = allProgress.stream().collect(Collectors.groupingBy(
                        p -> p.getTopic().getLanguage().getId(),
                        Collectors.summingLong(UserProgress::getTotalXP)
                ));

        return userLanguages.stream().map(ul -> {
            UUID languageId = ul.getLanguage().getId();
            Long totalXP = xpByLanguage.getOrDefault(languageId, 0L);
            Integer level = LevelUtils.calculateLevel(totalXP);

            return LanguageLevelResponse.builder()
                    .languageId(languageId)
                    .languageName(ul.getLanguage().getName())
                    .languageCode(ul.getLanguage().getCode())
                    .level(level)
                    .totalXP(totalXP)
                    .currentLevelXP(LevelUtils.calculateCurrentLevelXP(totalXP))
                    .nextLevelXP(LevelUtils.calculateNextLevelXP((long) level))
                    .levelProgressPercentage(LevelUtils.calculateLevelProgressPercentage(totalXP))
                    .build();
        }).collect(Collectors.toList());
    }


    @Override
    public Optional<LastLessonResponse> getLastStudiedLesson(UUID accountId) {
      Optional<UserLessonProgress> lastAttempt = userLessonProgressRepository.findLastAttemptByAccountId(accountId);
      if(lastAttempt.isEmpty()) {
        return Optional.empty();
      }
      Topic lastTopic = lastAttempt.get().getLesson().getTopic();
      UUID languageId = lastTopic.getLanguage().getId();

      Optional<Lesson> nextLessonUnCompleted = lessonRepository.findFirstUncompletedLessonInTopic(accountId, lastTopic.getId());
      Lesson nextLesson = null;

      if(nextLessonUnCompleted.isPresent()) {
        nextLesson = nextLessonUnCompleted.get();
      } else {
        List<Topic> topicsInLanguage = topicRepository.findByLanguage_IdAndIsActiveTrueOrderByDifficultyAscNameAsc(languageId);

        int currentTopicIndex = -1;
        for (int i = 0; i < topicsInLanguage.size(); i++) {
            if (topicsInLanguage.get(i).getId().equals(lastTopic.getId())) {
                currentTopicIndex = i;
                break;
            }
        }

        if (currentTopicIndex != -1) {
            for (int i = currentTopicIndex + 1; i < topicsInLanguage.size(); i++) {
                Topic nextTopic = topicsInLanguage.get(i);
                Optional<Lesson> unCompletedLesson = lessonRepository.findFirstUncompletedLessonInTopic(accountId, nextTopic.getId());
                if (unCompletedLesson.isPresent()) {
                    nextLesson = unCompletedLesson.get();
                    break;
                }
            }
        }
      }
      if(nextLesson == null) {
        return Optional.empty();    
      }
      Topic topic = nextLesson.getTopic();
      Integer completedCount = userLessonProgressRepository.countCompletedByAccountAndTopic(accountId, topic.getId());
      Integer totalLessons = lessonRepository.countByTopic_Id(topic.getId());

        return Optional.of(LastLessonResponse.builder()
            .lessonId(nextLesson.getId())
            .lessonName(nextLesson.getTitle())
            .lessonType(nextLesson.getLessonType())
            .xpReward(nextLesson.getXpReward())
            .topicId(topic.getId())
            .topicName(topic.getName())
            .languageName(topic.getLanguage().getName())
            .languageCode(topic.getLanguage().getCode())
            .completionCount(completedCount)
            .totalLessonsInTopic(totalLessons)
            .build());

        }
}
