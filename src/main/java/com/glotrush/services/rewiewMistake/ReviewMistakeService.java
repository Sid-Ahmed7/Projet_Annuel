package com.glotrush.services.rewiewMistake;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.glotrush.constants.MistakeQuestionConstants;
import com.glotrush.constants.TimeConstants;
import com.glotrush.dto.request.answer.UserAnswerRequest;
import com.glotrush.dto.request.answer.UserAnswerResultRequest;
import com.glotrush.dto.request.answer.UserMistakeAddMultipleRequest;
import com.glotrush.dto.request.answer.UserMistakeAddRequest;
import com.glotrush.dto.response.MistakeQuestionResponse;
import com.glotrush.dto.response.answers.UserDailyQuestion;
import com.glotrush.dto.response.answers.UserMistakeListResponse;
import com.glotrush.dto.response.answers.UserAnswerResponse;
import com.glotrush.dto.response.answers.UserMistakeRetryListResponse;
import com.glotrush.dto.response.answers.UserResultResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Topic;
import com.glotrush.entities.UserMistake;
import com.glotrush.entities.exercice.FlashcardEntity;
import com.glotrush.entities.exercice.MatchingPairEntity;
import com.glotrush.entities.exercice.QcmQuestionEntity;
import com.glotrush.entities.exercice.SortingExerciseEntity;
import com.glotrush.entities.exercice.InteractiveQuestionEntity;
import com.glotrush.enumerations.InteractiveSystemType;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.TopicRepository;
import com.glotrush.repositories.UserMistakeRepository;
import com.glotrush.services.loader.IQuestionLoaderService;
import com.glotrush.utils.LevenshteinUtils;
import com.glotrush.mapping.UserMistakeMapper;
import com.glotrush.mapping.model.LessonTypeMaps;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewMistakeService implements IReviewMistakeService {
    private final UserMistakeRepository userMistakeRepository;
    private final AccountsRepository accountsRepository;
    private final TopicRepository topicRepository;
    private final IQuestionLoaderService questionLoaderService;
    private final UserMistakeMapper userMistakeMapper;

    @Override
    public UserDailyQuestion getDailyMistakeQuestion(UUID accountId) {
        LocalDateTime now = LocalDateTime.now();
        List<UserMistake> pendingMistakes = userMistakeRepository
            .findPendingReviewMistakes(accountId, now)
            .stream()
            .limit(MistakeQuestionConstants.DAILY_MISTAKE_QUESTION_DISPLAY)
            .toList();

        long totalAvailableNow = userMistakeRepository.countPendingReviewMistakes(accountId, now);
        long totalPendingMistakes = userMistakeRepository.countByAccount_IdAndIsResolvedFalse(accountId);
        LessonTypeMaps lessonType = questionLoaderService.loadQuestionMaps(pendingMistakes);
        List<MistakeQuestionResponse> mistakeQuestionResponses = userMistakeMapper.toMistakeQuestionResponses(pendingMistakes, lessonType);

        return UserDailyQuestion.builder()
            .hasQuestionToday(!mistakeQuestionResponses.isEmpty())
            .totalQuestions(totalPendingMistakes)
            .totalAvailableNow(totalAvailableNow)
            .mistakeQuestions(mistakeQuestionResponses)
            .build();
    }

    @Override
    public UserResultResponse submitAnswer(UUID accountId, UserAnswerResultRequest answerResult) {
        List<UUID> mistakeIds = answerResult.getUserAnswers().stream().map(UserAnswerRequest::getUserMistakeId).toList();

        Map<UUID, UserMistake> mistakes = userMistakeRepository.findAllById(mistakeIds).stream()
            .filter(mistake -> mistake.getAccount().getId().equals(accountId))
            .collect(Collectors.toMap(UserMistake::getId, Function.identity()));

        LessonTypeMaps lessonType = questionLoaderService.loadQuestionMaps(new ArrayList<>(mistakes.values()));

        int correctAnswers = 0;
        int resolvedMistakes = 0;
        List<UserAnswerResponse> answerResults = new ArrayList<>();

        for (UserAnswerRequest answer : answerResult.getUserAnswers()) {
            UserMistake mistake = mistakes.get(answer.getUserMistakeId());
            if (mistake != null) {
                boolean isCorrect = validateAnswer(mistake, answer, lessonType);
                if (isCorrect) {
                    correctAnswers++;
                }

                if (!isCorrect) {
                    mistake.setUserAnswer(extractUserAnswer(mistake, answer, lessonType));
                }
                updateMistakeProgress(mistake, isCorrect);
                userMistakeRepository.save(mistake);

                if (Boolean.TRUE.equals(mistake.getIsResolved())){
                    resolvedMistakes++;
                }

                answerResults.add(UserAnswerResponse.builder()
                    .userMistakeId(mistake.getId())
                    .isAnswerCorrect(isCorrect)
                    .nbCorrectResponses(mistake.getConsecutiveGoodAnswers())
                    .isLessonMastered(Boolean.TRUE.equals(mistake.getIsResolved()))
                    .nextReviewIndication(userMistakeMapper.formatNextReview(mistake))
                    .build());
            }
        }

        long remaining = userMistakeRepository.countByAccount_IdAndIsResolvedFalse(accountId);

        return UserResultResponse.builder()
            .totalAnswers(answerResult.getUserAnswers().size())
            .totalCorrectAnswers(correctAnswers)
            .totalMasteredLessons(resolvedMistakes)
            .totalRemainingQuestions(remaining)
            .answerResults(answerResults)
            .build();
    }

    @Override
    public UserMistakeListResponse getPendingMistakesQuestion(UUID accountId) {
        List<UserMistake> pendingMistakes = userMistakeRepository.findByAccount_IdAndIsResolvedFalseOrderByCreatedAtAsc(accountId);
        LessonTypeMaps lessonType = questionLoaderService.loadQuestionMaps(pendingMistakes);
        List<UserMistakeRetryListResponse> mistakeResponses = userMistakeMapper.toRetryListResponses(pendingMistakes, lessonType);
        return UserMistakeListResponse.builder()
            .totalPendingMistakes(mistakeResponses.size())
            .mistakes(mistakeResponses)
            .build();
    }

    @Override
    public UserMistakeRetryListResponse getMistakeQuestionDetail(UUID accountId, UUID userMistakeId) {
        UserMistake userMistake = userMistakeRepository.findById(userMistakeId).filter(mistake -> mistake.getAccount().getId().equals(accountId))
            .orElseThrow(() -> new IllegalArgumentException("Mistake not found for the given account and ID"));

        LessonTypeMaps lessonType = questionLoaderService.loadQuestionMaps(List.of(userMistake));
        return userMistakeMapper.toRetryListResponse(userMistake, lessonType);
    }

    @Override
    public void addToMistakeList(UUID accountId, UserMistakeAddRequest request) {
        Optional<UserMistake> existing = userMistakeRepository.findByAccount_IdAndQuestionIdAndIsResolvedFalse(accountId, request.getQuestionId());
        if (existing.isPresent()) {
            UserMistake mistake = existing.get();
            if (request.getUserAnswer() != null) {
                mistake.setUserAnswer(request.getUserAnswer());
                userMistakeRepository.save(mistake);
            }
            return;
        }

        Accounts account = accountsRepository.getReferenceById(accountId);
        Topic topic = request.getTopicId() != null ? topicRepository.getReferenceById(request.getTopicId()) : null;

        UserMistake mistakes = UserMistake.builder()
            .account(account)
            .questionId(request.getQuestionId())
            .lessonType(request.getLessonType())
            .learningType(request.getLearningType())
            .topic(topic)
            .userAnswer(request.getUserAnswer())
            .build();

        userMistakeRepository.save(mistakes);
    }

    @Override
    public void addMultipleToMistakeList(UUID accountId, UserMistakeAddMultipleRequest request) {
        for(UUID questionId : request.getQuestionIds()) {
            UserMistakeAddRequest singleRequest = UserMistakeAddRequest.builder()
                .questionId(questionId)
                .lessonType(request.getLessonType())
                .learningType(request.getLearningType())
                .topicId(request.getTopicId())
                .build();

            addToMistakeList(accountId, singleRequest);
        }
  
    }

     private void updateMistakeProgress(UserMistake mistakeAnswer, boolean correct) {
        if (correct) {
            int consecutiveGoodAnswers = mistakeAnswer.getConsecutiveGoodAnswers() + 1;
            mistakeAnswer.setConsecutiveGoodAnswers(consecutiveGoodAnswers);

            double oldRate = mistakeAnswer.getRateMultiplier();
            double newRate = Math.min(3.0, oldRate + 0.1);
            mistakeAnswer.setRateMultiplier(newRate);

            int scheduledInterval = mistakeAnswer.getIntervalHours();
            int overdue = (int) Duration.between(mistakeAnswer.getNextReviewAt(), LocalDateTime.now()).toHours();
            int cappedOverdue = Math.min(Math.max(0, overdue), scheduledInterval);
            int baseInterval = scheduledInterval + cappedOverdue;
            int newInterval = (int) Math.round(baseInterval * oldRate);
            mistakeAnswer.setIntervalHours(newInterval);
            mistakeAnswer.setNextReviewAt(LocalDateTime.now().plusHours(newInterval));

            if (consecutiveGoodAnswers >= MistakeQuestionConstants.NUMBER_OF_CORRECT_ANSWERS_TO_RESOLVE_MISTAKE
                    && mistakeAnswer.getIntervalHours() >= MistakeQuestionConstants.MIN_INTERVAL_HOURS_TO_RESOLVE) {
                mistakeAnswer.setIsResolved(true);
                mistakeAnswer.setResolvedAt(LocalDateTime.now());
            }
        } else {
            mistakeAnswer.setConsecutiveGoodAnswers(0);
            double newRate = Math.max(MistakeQuestionConstants.MINIMUM_RATE, mistakeAnswer.getRateMultiplier() - 0.2);
            mistakeAnswer.setRateMultiplier(newRate);
            mistakeAnswer.setIntervalHours(TimeConstants.TWENTY_FOUR_HOURS);
            mistakeAnswer.setNextReviewAt(LocalDateTime.now());
        }
    }


    private String extractUserAnswer(UserMistake mistake, UserAnswerRequest answer, LessonTypeMaps lessonType) {
        UUID questionId = mistake.getQuestionId();
        return switch (mistake.getLessonType()) {
            case FLASHCARD -> answer.getTranslateAnswer();
            case QCM -> {
                QcmQuestionEntity qcm = lessonType.qcms().get(questionId);
                yield (qcm != null && answer.getSelectedResponseIndex() != null)
                    ? qcm.getOptions().get(answer.getSelectedResponseIndex())
                    : null;
            }
            case MATCHING_PAIR -> answer.getItem2();
            case SORTING_EXERCISE -> {
                SortingExerciseEntity sortingExercise = lessonType.sortingExercises().get(questionId);
                yield (sortingExercise != null && answer.getUserOrderedResponseIndexes() != null)
                    ? answer.getUserOrderedResponseIndexes().stream()
                        .map(i -> sortingExercise.getItems().get(i))
                        .collect(Collectors.joining(" → "))
                    : null;
            }
            case INTERACTIVE -> {
                InteractiveQuestionEntity interactiveQuestion = lessonType.interactiveQuestions().get(questionId);
                if (interactiveQuestion == null) {
                    yield null;
                }

                if (interactiveQuestion.getSystemType() == InteractiveSystemType.MULTIPLE_CHOICE) {
                    yield (answer.getSelectedResponseIndex() != null)
                        ? interactiveQuestion.getOptions().get(answer.getSelectedResponseIndex())
                        : null;
                } else {
                    yield answer.getTranslateAnswer();
                }
            }
        };
    }

    private boolean validateAnswer(UserMistake mistakeAnswer, UserAnswerRequest answer, LessonTypeMaps lessonType) {
        UUID questionId = mistakeAnswer.getQuestionId();
        return switch (mistakeAnswer.getLessonType()) {
            case FLASHCARD -> {
                FlashcardEntity flashCard = lessonType.flashcards().get(questionId);
                if (flashCard == null || answer.getTranslateAnswer() == null){
                    yield false;
                }

                String expected = flashCard.getBack().trim().toLowerCase();
                String actual = answer.getTranslateAnswer().trim().toLowerCase();
                yield expected.equals(actual) || LevenshteinUtils.calculateLevenshteinDistance(expected, actual) <= 2;
            }

            case QCM -> {
                QcmQuestionEntity qcm = lessonType.qcms().get(questionId);
                yield qcm != null && answer.getSelectedResponseIndex() != null && qcm.getCorrectOptionIndex().equals(answer.getSelectedResponseIndex());
            }

            case MATCHING_PAIR -> {
                MatchingPairEntity matchingPair = lessonType.matchingPairs().get(questionId);
                yield matchingPair != null && answer.getItem2() != null && matchingPair.getItem2().equals(answer.getItem2());
            }


            case SORTING_EXERCISE -> {
                SortingExerciseEntity sortingExercise = lessonType.sortingExercises().get(questionId);
                if (sortingExercise == null || sortingExercise.getCorrectOrder() == null || answer.getUserOrderedResponseIndexes() == null) {
                    yield false;
                }

                if (sortingExercise.getCorrectOrder().size() != answer.getUserOrderedResponseIndexes().size()) {
                    yield false;
                }

                List<Integer> userOrder = answer.getUserOrderedResponseIndexes();
                boolean allInBounds = userOrder.stream().allMatch(index -> index != null && index >= 0 && index < sortingExercise.getItems().size());
                if (!allInBounds) {
                    yield false;
                }

                List<String> expectedWords = sortingExercise.getCorrectOrder().stream()
                        .map(index -> sortingExercise.getItems().get(index))
                        .toList();

                List<String> actualWords = userOrder.stream()
                        .map(index -> sortingExercise.getItems().get(index))
                        .toList();

                yield expectedWords.equals(actualWords);
            }

            case INTERACTIVE -> {
                InteractiveQuestionEntity interactiveQuestion = lessonType.interactiveQuestions().get(questionId);
                if (interactiveQuestion == null) {
                    yield false;
                }
                if (interactiveQuestion.getSystemType() == InteractiveSystemType.MULTIPLE_CHOICE) {
                    yield answer.getSelectedResponseIndex() != null &&
                            interactiveQuestion.getCorrectOptionIndex().equals(answer.getSelectedResponseIndex());
                } else {
                    if (answer.getTranslateAnswer() == null) {
                        yield false;
                    }
                    String expected = interactiveQuestion.getCorrectWord().trim().toLowerCase();
                    String actual = answer.getTranslateAnswer().trim().toLowerCase();
                    yield expected.equals(actual) || LevenshteinUtils.calculateLevenshteinDistance(expected, actual) <= 2;
                }
            }
        };
    }
    
}
