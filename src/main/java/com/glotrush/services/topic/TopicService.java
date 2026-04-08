package com.glotrush.services.topic;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.glotrush.builder.LessonBuilder;
import com.glotrush.dto.response.*;
import com.glotrush.dto.response.exercice.*;
import com.glotrush.dto.response.lesson.*;
import com.glotrush.entities.Language;
import com.glotrush.entities.UserLessonProgress;
import com.glotrush.enumerations.LessonStatus;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.lesson.*;
import com.glotrush.enumerations.ProficiencyLevel;
import com.glotrush.mapping.LessonEntityToLessonResponse;
import com.glotrush.mapping.TopicMapper;
import com.glotrush.repositories.LanguageRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.UserLessonProgressRepository;
import com.glotrush.services.progress.IProgressService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.glotrush.entities.Topic;
import com.glotrush.entities.UserProgress;
import com.glotrush.exceptions.TopicNotFoundException;
import com.glotrush.builder.TopicBuilder;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.UserProgressRepository;
import com.glotrush.utils.LevelUtils;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;


import com.glotrush.dto.request.*;
import com.glotrush.entities.exercice.FlashcardEntity;
import com.glotrush.entities.exercice.MatchingPairEntity;
import com.glotrush.entities.exercice.QcmQuestionEntity;
import com.glotrush.entities.exercice.SortingExerciseEntity;
import com.glotrush.repositories.exercice.FlashcardRepository;
import com.glotrush.repositories.exercice.MatchingPairRepository;
import com.glotrush.repositories.exercice.QcmQuestionRepository;
import com.glotrush.repositories.exercice.SortingExerciseRepository;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class TopicService implements ITopicService {
    private final MessageSource messageSource;
    private final TopicRepository topicRepository;
    private final UserProgressRepository userProgressRepository;
    private final LanguageRepository languageRepository;
    private final LessonRepository lessonRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;
    private final LessonBuilder lessonBuilder;
    private final TopicBuilder topicBuilder;
    private final TopicMapper topicMapper;
    private final LessonEntityToLessonResponse lessonMapper;
    private final IProgressService progressService;

    private final FlashcardRepository flashcardRepository;
    private final QcmQuestionRepository qcmQuestionRepository;
    private final MatchingPairRepository matchingPairRepository;
    private final SortingExerciseRepository sortingExerciseRepository;

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    @Override
    public List<TopicResponse> getAllTopics(UUID accountId) {
        return topicRepository.findByIsActiveTrueOrderByOrderIndexAsc().stream()
                .map(topic -> {
                    Optional<UserProgress> progressOpt = userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topic.getId());
                    return topicBuilder.mapToTopicResponse(topic, progressOpt);
                })
                .toList();
    }

    @Override
    public List<TopicResponse> getAllTopics() {
        return topicRepository.findAll().stream()
                .map(topic -> topicMapper.mapTopicEntitiesToTopicResponse(topic))
                .toList();
    }

    @Override
    public List<TopicWithProgressResponse> getTopicsByLanguage(UUID languageId, UUID accountId) {
        return topicRepository.findByLanguage_IdAndIsActiveTrueOrderByOrderIndexAsc(languageId).stream().map(topic -> {
            List<LessonResponse> lessons = lessonRepository.findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topic.getId()).stream()
                    .map(lesson -> {
                        Optional<UserLessonProgress> progressOpt = accountId != null
                                ? userLessonProgressRepository.findByAccount_IdAndLesson_Id(accountId, lesson.getId())
                                : Optional.empty();
                        return lessonBuilder.mapLessonToLessonResponse(lesson, progressOpt);
                    })
                    .toList();

            int completedLessons = (int) lessons.stream()
                    .filter(l -> l.getUserProgress() != null && LessonStatus.COMPLETED.equals(l.getUserProgress().getStatus()))
                    .count();

            boolean exammUnlocked = lessons.size() > 0 && completedLessons == lessons.size();
            boolean examPassed = userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topic.getId()).map(UserProgress::getExamPassed).orElse(false);
                    
            return topicBuilder.mapToTopicWithProgressResponse(topic, lessons, completedLessons, exammUnlocked, examPassed);
        }).toList();
    }

    @Override
    public TopicResponse getTopicById(UUID topicId, UUID accountId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale())));
        Optional<UserProgress> progressOpt = userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId);
        return topicBuilder.mapToTopicResponse(topic, progressOpt);
    }

    @Override
    public TopicResponse createTopic(TopicRequest topicRequest) {
        Language language = languageRepository.findById(topicRequest.getLanguageId())
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.language_notfound", null, LocaleUtils.getCurrentLocale())));
        
        Topic topicEntity = topicMapper.mapTopicRequestToMapTopicEntities(topicRequest);
        topicEntity.setLanguage(language);
        
        topicRepository.save(topicEntity);
        return topicMapper.mapTopicEntitiesToTopicResponse(topicEntity);
    }

    @Override
    public void removeTopic(UUID topicId) {
        if(!topicRepository.existsById(topicId)) {
            throw new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale()));
        }
        topicRepository.deleteById(topicId);
    }

    @Override
    public TopicResponse updateTopic(UUID topicId, TopicRequest topicRequest) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale())));

        if (topicRequest.getLanguageId() != null && !topicRequest.getLanguageId().equals(topic.getLanguage().getId())) {
            Language language = languageRepository.findById(topicRequest.getLanguageId())
                    .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.language_notfound", null, LocaleUtils.getCurrentLocale())));
            topic.setLanguage(language);
        }

        topicMapper.updateTopicFromRequest(topicRequest, topic);
        
        return topicMapper.mapTopicEntitiesToTopicResponse(topicRepository.save(topic));
    }

    @Override
    public List<TopicResponse> searchTopics(String name, ProficiencyLevel difficulty, Boolean isActive) {
        Specification<Topic> spec = createSearchSpecification(null, name, difficulty, isActive);

        return topicRepository.findAll(spec).stream()
                .map(topicMapper::mapTopicEntitiesToTopicResponse)
                .toList();
    }

    @Override
    public List<TopicResponse> searchActiveTopics(UUID languageId, String name, ProficiencyLevel difficulty) {
        Specification<Topic> spec = createSearchSpecification(languageId, name, difficulty, true);

        return topicRepository.findAll(spec).stream()
                .map(topicMapper::mapTopicEntitiesToTopicResponse)
                .toList();
    }

    private Specification<Topic> createSearchSpecification(UUID languageId, String name, ProficiencyLevel difficulty, Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (languageId != null) {
                predicates.add(criteriaBuilder.equal(root.get("language").get("id"), languageId));
            }

            if (name != null && !name.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (difficulty != null) {
                predicates.add(criteriaBuilder.equal(root.get("difficulty"), difficulty));
            }

            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public ExamResponse generateTopicExam(UUID accountId, UUID topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale())));

        // Vérifier si toutes les leçons sont terminées
        List<Lesson> lessons = lessonRepository.findByTopic_IdAndIsActiveTrueOrderByOrderIndexAsc(topicId);
        long completedCount = userLessonProgressRepository.findByAccount_Id(accountId).stream()
                .filter(p -> p.getLesson().getTopic().getId().equals(topicId) && p.getStatus() == LessonStatus.COMPLETED)
                .count();

        if (completedCount < lessons.size()) {
            throw new IllegalStateException(messageSource.getMessage("error.topic.exam.not_ready", null, LocaleUtils.getCurrentLocale()));
        }

        List<QcmQuestionExamResponse> qcmQuestions = new ArrayList<>();
        List<FlashcardExamResponse> flashcards = new ArrayList<>();
        List<MatchingPairResponse> matchingPairs = new ArrayList<>();
        List<SortingExerciseExamResponse> sortingExercises = new ArrayList<>();
        for (Lesson lesson : lessons) {
            if (Boolean.FALSE.equals(lesson.getIsIncludedInExam())) continue;

            if (lesson instanceof QcmLesson qcmLesson) {
                qcmQuestions.addAll(qcmLesson.getQuestions().stream()
                        .map(lessonMapper::mapQcmQuestionEntityToQcmQuestionExamResponse)
                        .toList());
            } else if (lesson instanceof FlashcardLesson flashcardLesson) {
                flashcards.addAll(flashcardLesson.getFlashcards().stream()
                        .map(lessonMapper::mapFlashcardEntityToFlashcardExamResponse)
                        .toList());
            } else if (lesson instanceof MatchingPairLesson matchingPairLesson) {
                LessonResponse resp = lessonMapper.lessonEntityToLessonResponse(matchingPairLesson, messageSource);
                if (resp instanceof MatchingPairLessonResponse mpResp) {
                    matchingPairs.addAll(mpResp.getMatchingPairs());
                }
            } else if (lesson instanceof SortingExerciseLesson sortingLesson) {
                sortingExercises.addAll(sortingLesson.getSortingExercise().stream()
                        .map(lessonMapper::mapSortingExerciseEntityToSortingExerciseExamResponse)
                        .toList());
            }
        }

        Collections.shuffle(qcmQuestions);
        Collections.shuffle(flashcards);
        Collections.shuffle(matchingPairs);
        Collections.shuffle(sortingExercises);

        // Limiter à un certain nombre (ex: 5 de chaque pour un examen varié)
        return ExamResponse.builder()
                .topicId(topicId)
                .topicName(topic.getName())
                .qcmQuestions(qcmQuestions.stream().limit(5).collect(Collectors.toList()))
                .flashcards(flashcards.stream().limit(5).collect(Collectors.toList()))
                .matchingPairs(matchingPairs.stream().limit(5).collect(Collectors.toList()))
                .sortingExercises(sortingExercises.stream().limit(5).collect(Collectors.toList()))
                .build();
    }

    @Override
    public CompleteExamResponse completeTopicExam(UUID accountId, UUID topicId, ExamResultRequest examRequest) {
        UserProgress progress = userProgressRepository.findByAccount_IdAndTopic_Id(accountId, topicId)
                .orElseThrow(() -> new TopicNotFoundException(messageSource.getMessage("error.topic.notfound", null, LocaleUtils.getCurrentLocale())));

        int calculatedCorrectAnswers = 0;
        int totalQuestions = 0;

        // Validation des Flashcards
        if (examRequest.getFlashcardAnswers() != null) {
            for (FlashcardAnswerRequest ans : examRequest.getFlashcardAnswers()) {
                totalQuestions++;
                if (validateFlashcard(ans)) {
                    calculatedCorrectAnswers++;
                }
            }
        }

        // Validation des QCM
        if (examRequest.getQcmAnswers() != null) {
            for (QcmAnswerRequest ans : examRequest.getQcmAnswers()) {
                totalQuestions++;
                if (validateQcm(ans)) {
                    calculatedCorrectAnswers++;
                }
            }
        }

        // Validation des Matching Pairs
        if (examRequest.getMatchingPairAnswers() != null) {
            for (MatchingPairAnswerRequest ans : examRequest.getMatchingPairAnswers()) {
                totalQuestions++;
                if (validateMatchingPair(ans)) {
                    calculatedCorrectAnswers++;
                }
            }
        }

        // Validation des Sorting Exercises
        if (examRequest.getSortingExerciseAnswers() != null) {
            for (SortingExerciseAnswerRequest ans : examRequest.getSortingExerciseAnswers()) {
                totalQuestions++;
                if (validateSortingExercise(ans)) {
                    calculatedCorrectAnswers++;
                }
            }
        }

        double finalScore = totalQuestions > 0 ? (double) calculatedCorrectAnswers / totalQuestions * 100 : 0;
        boolean isSuccessful = finalScore >= 80;

        int xpEarned = isSuccessful ? 50 : 0;
        Long oldXP = progress.getTotalXP();
        Integer oldLevel = LevelUtils.calculateLevel(oldXP);

        if (isSuccessful) {
            boolean isFirstTimePassing = !progress.getExamPassed();
            progress.setExamPassed(true);
            if (progress.getBestExamScore() == null || finalScore > progress.getBestExamScore()) {
                progress.setBestExamScore(finalScore);
            }
            if (isFirstTimePassing) {
                progress.setTotalXP(oldXP + xpEarned);
            }
        }

        progress.setExamAttempts(progress.getExamAttempts() + 1);
        progress.setCorrectAnswers(progress.getCorrectAnswers() + calculatedCorrectAnswers);
        progress.setTotalAnswers(progress.getTotalAnswers() + totalQuestions);
        progress.calculateAccuracy();

        userProgressRepository.save(progress);

        Long newXP = progress.getTotalXP();
        Integer newLevel = LevelUtils.calculateLevel(newXP);
        boolean leveledUp = newLevel > oldLevel;

        return CompleteExamResponse.builder()
                .success(isSuccessful)
                .message(isSuccessful ? messageSource.getMessage("info.topic.exam.success", null, LocaleUtils.getCurrentLocale()) : messageSource.getMessage("error.topic.exam.failed", null, LocaleUtils.getCurrentLocale()))
                .xpEarned(xpEarned)
                .totalXP(newXP)
                .currentLevel(newLevel)
                .leveledUp(leveledUp)
                .newLevel(newLevel)
                .totalAnswers(totalQuestions)
                .correctAnswers(calculatedCorrectAnswers)
                .accuracy(totalQuestions > 0 ? (double) calculatedCorrectAnswers / totalQuestions : 0.0)
                .build();
    }

    private boolean validateFlashcard(FlashcardAnswerRequest request) {
        FlashcardEntity entity = flashcardRepository.findById(request.getId()).orElse(null);
        if (entity == null || request.getUserResponse() == null) return false;

        String expected = entity.getBack().trim();
        String actual = request.getUserResponse().trim();

        // Niveau 1 : Comparaison exacte
        if (expected.equalsIgnoreCase(actual)) {
            return true;
        }

        // Niveau 2 : Extraction des nombres
        List<String> expectedNumbers = extractNumbers(expected);
        List<String> actualNumbers = extractNumbers(actual);
        if (!expectedNumbers.equals(actualNumbers)) {
            return false;
        }

        // Niveau 3 : IA (Simulé ici par Levenshtein car IA est optionnelle)
        // Si les nombres sont bons mais que le texte ne match pas exactement, on vérifie la distance
        return calculateLevenshteinDistance(expected.toLowerCase(), actual.toLowerCase()) <= 2;
    }

    private List<String> extractNumbers(String input) {
        List<String> numbers = new ArrayList<>();
        Matcher matcher = NUMBER_PATTERN.matcher(input);
        while (matcher.find()) {
            numbers.add(matcher.group());
        }
        return numbers;
    }

    private int calculateLevenshteinDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];
        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) dp[i][j] = j;
                else if (j == 0) dp[i][j] = i;
                else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + (x.charAt(i - 1) == y.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }
        return dp[x.length()][y.length()];
    }

    private boolean validateQcm(QcmAnswerRequest request) {
        QcmQuestionEntity entity = qcmQuestionRepository.findById(request.getId()).orElse(null);
        return entity != null && entity.getCorrectOptionIndex().equals(request.getSelectedOptionIndex());
    }

    private boolean validateMatchingPair(MatchingPairAnswerRequest request) {
        MatchingPairEntity entity = matchingPairRepository.findById(request.getId()).orElse(null);
        if (entity == null) return false;
        // Vérification exacte
        return entity.getItem1() != null && entity.getItem2() != null && entity.getItem1().equals(request.getItem1()) && entity.getItem2().equals(request.getItem2());
    }

    private boolean validateSortingExercise(SortingExerciseAnswerRequest request) {
        SortingExerciseEntity entity = sortingExerciseRepository.findById(request.getId()).orElse(null);
        if (entity == null || request.getUserOrder() == null) return false;
        // Vérification exacte de l'ordre
        return entity.getCorrectOrder() != null && entity.getCorrectOrder().equals(request.getUserOrder());
    }
}
