package com.glotrush.builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.glotrush.dto.request.challenge.ChallengeInteractiveRequest;
import com.glotrush.dto.request.challenge.CreateChallengeRequest;
import com.glotrush.dto.response.challenge.ChallengeFlashCardResponse;
import com.glotrush.dto.response.challenge.ChallengeInteractiveResponse;
import com.glotrush.dto.response.challenge.ChallengeMatchingPairResponse;
import com.glotrush.dto.response.challenge.ChallengeParticipantResponse;
import com.glotrush.dto.response.challenge.ChallengeQcmResponse;
import com.glotrush.dto.response.challenge.ChallengeResponse;
import com.glotrush.dto.response.challenge.ChallengeSortingExerciseResponse;
import com.glotrush.dto.response.challenge.ChallengeUserResponse;
import com.glotrush.dto.response.challenge.ChallengeProgress;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Language;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.UserProfile;
import com.glotrush.entities.challenge.Challenge;
import com.glotrush.entities.challenge.ChallengeFlashCard;
import com.glotrush.entities.challenge.ChallengeInteractive;
import com.glotrush.entities.challenge.ChallengeMatchingPair;
import com.glotrush.entities.challenge.ChallengeParticipant;
import com.glotrush.entities.challenge.ChallengeQcm;
import com.glotrush.entities.challenge.ChallengeSortingExercise;
import com.glotrush.entities.exercice.FlashcardEntity;
import com.glotrush.entities.exercice.InteractiveQuestionEntity;
import com.glotrush.entities.exercice.MatchingPairEntity;
import com.glotrush.entities.exercice.QcmQuestionEntity;
import com.glotrush.entities.exercice.SortingExerciseEntity;
import com.glotrush.entities.lesson.FlashcardLesson;
import com.glotrush.entities.lesson.InteractiveLesson;
import com.glotrush.entities.lesson.MatchingPairLesson;
import com.glotrush.entities.lesson.QcmLesson;
import com.glotrush.entities.lesson.SortingExerciseLesson;
import com.glotrush.enumerations.ChallengeStatus;
import com.glotrush.enumerations.ChallengeType;
import com.glotrush.enumerations.LessonType;
import com.glotrush.dto.request.LessonRequest;
import com.glotrush.dto.request.lesson.QcmLessonRequest;
import com.glotrush.dto.request.lesson.FlashcardLessonRequest;
import com.glotrush.dto.request.lesson.MatchingPairLessonRequest;
import com.glotrush.dto.request.lesson.SortingExerciseLessonRequest;
import com.glotrush.dto.request.lesson.InteractiveLessonRequest;
import com.glotrush.dto.response.ai.AIChallengeContentResponse;
import com.glotrush.dto.request.challenge.ChallengeFlashCardRequest;
import com.glotrush.dto.request.challenge.ChallengeMatchingPairRequest;
import com.glotrush.dto.request.challenge.ChallengeSortingExerciseRequest;
import com.glotrush.dto.request.challenge.ChallengeQcmRequest;

import org.springframework.stereotype.Component;

@Component
public class ChallengeBuilder {

    public Challenge buildChallenge(CreateChallengeRequest newChallenge, Lesson lesson, Language language, Language sourceLanguage, Accounts challenger, Accounts challenged) {
        ChallengeStatus status = newChallenge.getChallengeType() == ChallengeType.PUBLIC ? ChallengeStatus.ACTIVE : ChallengeStatus.PENDING;
        LocalDateTime expiresAt = newChallenge.getChallengeType() == ChallengeType.DUEL ? LocalDateTime.now().plusMinutes(10) : LocalDateTime.now().plusHours(24);

        Language languages = lesson != null ? lesson.getTopic().getTargetLanguage() : language;
        LessonType lessonType = lesson != null ? lesson.getLessonType() : newChallenge.getLessonType();

        Challenge challenge = Challenge.builder()
                .challenger(challenger)
                .challenged(challenged)
                .language(languages)
                .sourceLanguage(sourceLanguage)
                .challengeType(newChallenge.getChallengeType())
                .lessonType(lessonType)
                .challengeStatus(status)
                .title(newChallenge.getTitle())
                .expiresAt(expiresAt)
                .build();

        if (newChallenge.getChallengeType() == ChallengeType.DUEL) {
            buildQuestionsFromLesson(challenge, lesson, newChallenge.getQuestionCount());
        } else {
            buildQuestionsFromRequest(challenge, newChallenge);
        }

        return challenge;
    }

    private void buildQuestionsFromLesson(Challenge challenge, Lesson lesson, Integer questionCount) {
        switch (lesson.getLessonType()) {
            case QCM -> {
                List<QcmQuestionEntity> questions = new ArrayList<>(((QcmLesson) lesson).getQuestions());
                Collections.shuffle(questions);
                int nbQuestions = (questionCount != null && questionCount > 0) ? questionCount : questions.size();
                challenge.setQcm(questions.stream().limit(nbQuestions).map(qcm ->
                    ChallengeQcm.builder()
                        .challenge(challenge)
                        .question(qcm.getQuestion())
                        .options(new ArrayList<>(qcm.getOptions()))
                        .correctOptionIndex(qcm.getCorrectOptionIndex())
                        .explanation(qcm.getExplanation())
                        .build()
                ).collect(Collectors.toList()));
            }
            case FLASHCARD -> {
                List<FlashcardEntity> flashcards = new ArrayList<>(((FlashcardLesson) lesson).getFlashcards());
                Collections.shuffle(flashcards);
                int nbQuestions = (questionCount != null && questionCount > 0) ? questionCount : flashcards.size();
                challenge.setFlashcards(flashcards.stream().limit(nbQuestions).map(flashcard ->
                    ChallengeFlashCard.builder()
                        .challenge(challenge)
                        .front(flashcard.getFront())
                        .back(flashcard.getBack())
                        .frontLanguage(flashcard.getFrontLanguage())
                        .backLanguage(flashcard.getBackLanguage())
                        .timeLimitSeconds(30)
                        .build()
                ).collect(Collectors.toList()));
            }
            case MATCHING_PAIR -> {
                List<MatchingPairEntity> pairs = new ArrayList<>(((MatchingPairLesson) lesson).getMatchingPairs());
                Collections.shuffle(pairs);
                int nbQuestions = (questionCount != null && questionCount > 0) ? questionCount : pairs.size();
                challenge.setMatchingPairs(pairs.stream().limit(nbQuestions).map(pair ->
                    ChallengeMatchingPair.builder()
                        .challenge(challenge)
                        .item1(pair.getItem1())
                        .item2(pair.getItem2())
                        .build()
                ).collect(Collectors.toList()));
            }
            case SORTING_EXERCISE -> {
                List<SortingExerciseEntity> exercises = new ArrayList<>(((SortingExerciseLesson) lesson).getSortingExercise());
                Collections.shuffle(exercises);
                int nbQuestions = (questionCount != null && questionCount > 0) ? questionCount : exercises.size();
                challenge.setSortingExercises(exercises.stream().limit(nbQuestions).map(exercise ->
                    ChallengeSortingExercise.builder()
                        .challenge(challenge)
                        .items(exercise.getItems())
                        .correctOrder(exercise.getCorrectOrder())
                        .build()
                ).collect(Collectors.toList()));
            }
            case INTERACTIVE -> {
                List<InteractiveQuestionEntity> questions = new ArrayList<>(((InteractiveLesson) lesson).getQuestions());
                Collections.shuffle(questions);
                int nbQuestions = (questionCount != null && questionCount > 0) ? questionCount : questions.size();
                challenge.setInteractives(questions.stream().limit(nbQuestions).map(question ->
                    ChallengeInteractive.builder()
                        .challenge(challenge)
                        .questionText(question.getQuestionText())
                        .imagePaths(new ArrayList<>(question.getImagePaths()))
                        .audioPaths(new ArrayList<>(question.getAudioPaths()))
                        .systemType(question.getSystemType())
                        .options(new ArrayList<>(question.getOptions()))
                        .correctOptionIndex(question.getCorrectOptionIndex())
                        .correctWord(question.getCorrectWord())
                        .build()
                ).collect(Collectors.toList()));
            }
        }
    }

    private void buildQuestionsFromRequest(Challenge challenge, CreateChallengeRequest request) {
        if (request.getQcm() != null) {
            challenge.setQcm(request.getQcm().stream().map(qcm ->
                ChallengeQcm.builder()
                    .challenge(challenge)
                    .question(qcm.getQuestion())
                    .options(new ArrayList<>(qcm.getOptions()))
                    .correctOptionIndex(qcm.getCorrectOptionIndex())
                    .explanation(qcm.getExplanation())
                    .build()
            ).collect(Collectors.toList()));
        }
        if (request.getFlashcards() != null) {
            challenge.setFlashcards(request.getFlashcards().stream().map(flashCard ->
                ChallengeFlashCard.builder()
                    .challenge(challenge)
                    .front(flashCard.getFront())
                    .back(flashCard.getBack())
                    .frontLanguage(flashCard.getFrontLanguage())
                    .backLanguage(flashCard.getBackLanguage())
                    .timeLimitSeconds(flashCard.getTimeLimitSeconds() != null ? flashCard.getTimeLimitSeconds() : 30)
                    .build()
            ).collect(Collectors.toList()));
        }
        if (request.getMatchingPairs() != null) {
            challenge.setMatchingPairs(request.getMatchingPairs().stream().map(pair ->
                ChallengeMatchingPair.builder()
                    .challenge(challenge)
                    .item1(pair.getItem1())
                    .item2(pair.getItem2())
                    .build()
            ).collect(Collectors.toList()));
        }
        if (request.getSortingExercises() != null) {
            challenge.setSortingExercises(request.getSortingExercises().stream().map(exercise ->
                ChallengeSortingExercise.builder()
                    .challenge(challenge)
                    .items(exercise.getItems())
                    .correctOrder(exercise.getCorrectOrder())
                    .build()
            ).collect(Collectors.toList()));
        }
        if (request.getInteractives() != null) {
            challenge.setInteractives(request.getInteractives().stream().map(interactive ->
                ChallengeInteractive.builder()
                    .challenge(challenge)
                    .questionText(interactive.getQuestionText())
                    .imagePaths(interactive.getImagePaths() != null ? new ArrayList<>(interactive.getImagePaths()) : new ArrayList<>())
                    .audioPaths(interactive.getAudioPaths() != null ? new ArrayList<>(interactive.getAudioPaths()) : new ArrayList<>())
                    .systemType(interactive.getSystemType())
                    .options(interactive.getOptions() != null ? new ArrayList<>(interactive.getOptions()) : new ArrayList<>())
                    .correctOptionIndex(interactive.getCorrectOptionIndex())
                    .correctWord(interactive.getCorrectWord())
                    .build()
            ).collect(Collectors.toList()));
        }
    }

    public ChallengeParticipant buildParticipant(Challenge challenge, Accounts account) {
        return ChallengeParticipant.builder()
                .challenge(challenge)
                .account(account)
                .build();
    }


    public ChallengeResponse toResponse(Challenge challenge, UUID currentAccountId, UserProfile challengerProfile, UserProfile challengedProfile, Map<UUID, UserProfile> participantProfiles) {
        List<ChallengeParticipantResponse> participants = challenge.getParticipants().stream().map(participant -> toParticipantResponse(participant, challenge, currentAccountId,participantProfiles.get(participant.getAccount().getId())))
                .collect(Collectors.toList());

        return ChallengeResponse.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .challengeType(challenge.getChallengeType())
                .lessonType(challenge.getLessonType())
                .challengeStatus(challenge.getChallengeStatus())
                .languageId(challenge.getLanguage().getId())
                .sourceLanguageId(challenge.getSourceLanguage() != null ? challenge.getSourceLanguage().getId() : null)
                .sourceLanguageCode(challenge.getSourceLanguage() != null ? challenge.getSourceLanguage().getCode() : null)
                .sourceLanguageName(challenge.getSourceLanguage() != null ? challenge.getSourceLanguage().getName() : null)
                .challenger(toUserResponse(challenge.getChallenger(), challengerProfile))
                .challenged(challenge.getChallenged() != null ? toUserResponse(challenge.getChallenged(), challengedProfile) : null)
                .qcm(challenge.getQcm().stream().map(this::toQcmResponse).collect(Collectors.toList()))
                .flashcards(challenge.getFlashcards().stream().map(this::toFlashCardResponse).collect(Collectors.toList()))
                .matchingPairs(challenge.getMatchingPairs().stream().map(this::toMatchingPairResponse).collect(Collectors.toList()))
                .sortingExercises(challenge.getSortingExercises().stream().map(this::toSortingExerciseResponse).collect(Collectors.toList()))
                .interactives(challenge.getInteractives().stream().map(this::toInteractiveResponse).collect(Collectors.toList()))
                .participants(participants)
                .expiresAt(challenge.getExpiresAt())
                .createdAt(challenge.getCreatedAt())
                .build();
    }

    public ChallengeUserResponse toUserResponse(Accounts account, UserProfile profile) {
        return ChallengeUserResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .photoUrl(profile != null ? profile.getPhotoUrl() : null)
                .build();
    }

    public ChallengeParticipantResponse toParticipantResponse(ChallengeParticipant participant, Challenge challenge, UUID currentAccountId,UserProfile profile) {
        boolean showScore = Boolean.TRUE.equals(participant.getScoreRecorded()) || challenge.getChallengeType() == ChallengeType.PUBLIC || participant.getAccount().getId().equals(currentAccountId);
        return ChallengeParticipantResponse.builder()
                .accountId(participant.getAccount().getId())
                .username(participant.getAccount().getUsername())
                .photoUrl(profile != null ? profile.getPhotoUrl() : null)
                .score(showScore ? participant.getScore() : null)
                .timePassed(showScore ? participant.getTimePassed() : null)
                .xpGained(participant.getXpGained() != null ? participant.getXpGained().intValue() : null)
                .finalRank(showScore ? participant.getFinalRank() : null)
                .completedAt(participant.getCompletedAt())
                .hasCompleted(participant.getCompletedAt() != null)
                .build();
    }

    private ChallengeQcmResponse toQcmResponse(ChallengeQcm qcm) {
        return ChallengeQcmResponse.builder()
                .id(qcm.getId())
                .question(qcm.getQuestion())
                .options(qcm.getOptions())
                .correctOptionIndex(qcm.getCorrectOptionIndex())
                .explanation(qcm.getExplanation())
                .build();
    }

    private ChallengeFlashCardResponse toFlashCardResponse(ChallengeFlashCard flashCard) {
        return ChallengeFlashCardResponse.builder()
                .id(flashCard.getId())
                .front(flashCard.getFront())
                .back(flashCard.getBack())
                .frontLanguage(flashCard.getFrontLanguage())
                .backLanguage(flashCard.getBackLanguage())
                .timeLimitSeconds(flashCard.getTimeLimitSeconds())
                .build();
    }

    private ChallengeMatchingPairResponse toMatchingPairResponse(ChallengeMatchingPair matchingPair) {
        return ChallengeMatchingPairResponse.builder()
                .id(matchingPair.getId())
                .item1(matchingPair.getItem1())
                .item2(matchingPair.getItem2())
                .build();
    }

    private ChallengeSortingExerciseResponse toSortingExerciseResponse(ChallengeSortingExercise sortingExercise) {
        return ChallengeSortingExerciseResponse.builder()
                .id(sortingExercise.getId())
                .items(sortingExercise.getItems())
                .correctOrder(sortingExercise.getCorrectOrder())
                .build();
    }

    private ChallengeInteractiveResponse toInteractiveResponse(ChallengeInteractive interactive) {
        return ChallengeInteractiveResponse.builder()
                .id(interactive.getId())
                .questionText(interactive.getQuestionText())
                .imagePaths(interactive.getImagePaths())
                .audioPaths(interactive.getAudioPaths())
                .systemType(interactive.getSystemType())
                .options(interactive.getOptions())
                .correctOptionIndex(interactive.getCorrectOptionIndex())
                .correctWord(interactive.getCorrectWord())
                .build();
    }

    public ChallengeProgress toProgressResponse(ChallengeParticipant participant, UUID challengeId, UserProfile profile) {
        return ChallengeProgress.builder()
                .challengeId(challengeId)
                .accountId(participant.getAccount().getId())
                .username(participant.getAccount().getUsername())
                .photoUrl(profile != null ? profile.getPhotoUrl() : null)
                .score(participant.getScore())
                .timePassed(participant.getTimePassed())
                .finalRank(participant.getFinalRank())
                .xpGained(participant.getXpGained() != null ? participant.getXpGained().intValue() : null)
                .build();
    }

    public ChallengeProgress toProgressResponse(UUID challengeId, Accounts account, UserProfile profile, Double score, Integer questionsAnswered, Long timePassed, Integer totalQuestions) {
        return ChallengeProgress.builder()
                .challengeId(challengeId)
                .accountId(account.getId())
                .username(account.getUsername())
                .photoUrl(profile != null ? profile.getPhotoUrl() : null)
                .score(score)
                .questionAnswered(questionsAnswered)
                .timePassed(timePassed)
                .totalQuestions(totalQuestions)
                .build();
    }

    public AIChallengeContentResponse toAIChallengeContentResponse(LessonRequest lessonRequest) {
        AIChallengeContentResponse response = new AIChallengeContentResponse();
        response.setLessonType(lessonRequest.getLessonType());

        if (lessonRequest instanceof QcmLessonRequest qcmLessonRequest) {
            response.setQcm(qcmLessonRequest.getQuestions().stream().map(qcm -> {
                ChallengeQcmRequest req = new ChallengeQcmRequest();
                req.setQuestion(qcm.getQuestion());
                req.setOptions(new ArrayList<>(qcm.getOptions()));
                req.setCorrectOptionIndex(qcm.getCorrectOptionIndex());
                req.setExplanation(qcm.getExplanation());
                return req;
            }).collect(Collectors.toList()));
        } else if (lessonRequest instanceof FlashcardLessonRequest flashcardLessonRequest) {
            response.setFlashcards(flashcardLessonRequest.getFlashcards().stream().map(fc -> {
                ChallengeFlashCardRequest req = new ChallengeFlashCardRequest();
                req.setFront(fc.getFront());
                req.setBack(fc.getBack());
                req.setFrontLanguage(fc.getFrontLanguage());
                req.setBackLanguage(fc.getBackLanguage());
                req.setTimeLimitSeconds(30);
                return req;
            }).collect(Collectors.toList()));
        } else if (lessonRequest instanceof MatchingPairLessonRequest matchingPairLessonRequest) {
            response.setMatchingPairs(matchingPairLessonRequest.getMatchingPairs().stream().map(mp -> {
                ChallengeMatchingPairRequest req = new ChallengeMatchingPairRequest();
                req.setItem1(mp.getItem1());
                req.setItem2(mp.getItem2());
                return req;
            }).collect(Collectors.toList()));
        } else if (lessonRequest instanceof SortingExerciseLessonRequest sortingExerciseLessonRequest) {
            response.setSortingExercises(sortingExerciseLessonRequest.getSortingExercise().stream().map(se -> {
                ChallengeSortingExerciseRequest req = new ChallengeSortingExerciseRequest();
                req.setItems(new ArrayList<>(se.getItems()));
                req.setCorrectOrder(new ArrayList<>(se.getCorrectOrder()));
                return req;
            }).collect(Collectors.toList()));
        } else if (lessonRequest instanceof InteractiveLessonRequest interactiveLessonRequest) {
            response.setInteractives(interactiveLessonRequest.getInteractiveQuestions().stream().map(inter -> {
                ChallengeInteractiveRequest req = new ChallengeInteractiveRequest();
                req.setQuestionText(inter.getQuestionText());
                req.setImagePaths(new ArrayList<>(inter.getImagePaths()));
                req.setAudioPaths(new ArrayList<>(inter.getAudioPaths()));
                req.setSystemType(inter.getSystemType());
                req.setOptions(new ArrayList<>(inter.getOptions()));
                req.setCorrectOptionIndex(inter.getCorrectOptionIndex());
                req.setCorrectWord(inter.getCorrectWord());
                return req;
            }).collect(Collectors.toList()));
        }

        return response;
    }
}
