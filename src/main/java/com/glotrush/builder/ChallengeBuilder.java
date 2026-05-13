package com.glotrush.builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.glotrush.dto.request.challenge.CreateChallengeRequest;
import com.glotrush.dto.response.challenge.ChallengeFlashCardResponse;
import com.glotrush.dto.response.challenge.ChallengeMatchingPairResponse;
import com.glotrush.dto.response.challenge.ChallengeParticipantResponse;
import com.glotrush.dto.response.challenge.ChallengeQcmResponse;
import com.glotrush.dto.response.challenge.ChallengeResponse;
import com.glotrush.dto.response.challenge.ChallengeSortingExerciseResponse;
import com.glotrush.dto.response.challenge.ChallengeUserResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.UserProfile;
import com.glotrush.entities.challenge.Challenge;
import com.glotrush.entities.challenge.ChallengeFlashCard;
import com.glotrush.entities.challenge.ChallengeMatchingPair;
import com.glotrush.entities.challenge.ChallengeParticipant;
import com.glotrush.entities.challenge.ChallengeQcm;
import com.glotrush.entities.challenge.ChallengeSortingExercise;
import com.glotrush.entities.exercice.FlashcardEntity;
import com.glotrush.entities.exercice.MatchingPairEntity;
import com.glotrush.entities.exercice.QcmQuestionEntity;
import com.glotrush.entities.exercice.SortingExerciseEntity;
import com.glotrush.entities.lesson.FlashcardLesson;
import com.glotrush.entities.lesson.MatchingPairLesson;
import com.glotrush.entities.lesson.QcmLesson;
import com.glotrush.entities.lesson.SortingExerciseLesson;
import com.glotrush.enumerations.ChallengeStatus;
import com.glotrush.enumerations.ChallengeType;

import org.springframework.stereotype.Component;

@Component
public class ChallengeBuilder {

    public Challenge buildChallenge(CreateChallengeRequest newChallenge, Lesson lesson, Accounts challenger, Accounts challenged) {
        ChallengeStatus status = newChallenge.getChallengeType() == ChallengeType.PUBLIC ? ChallengeStatus.ACTIVE : ChallengeStatus.PENDING;

        LocalDateTime expiresAt = newChallenge.getChallengeType() == ChallengeType.DUEL ? LocalDateTime.now().plusMinutes(10) : LocalDateTime.now().plusHours(24);

        int count = newChallenge.getChallengeType() == ChallengeType.DUEL ? 1 : (newChallenge.getQuestionCount() != null ? newChallenge.getQuestionCount() : Integer.MAX_VALUE);

        Challenge challenge = Challenge.builder()
                .challenger(challenger)
                .challenged(challenged)
                .language(lesson.getTopic().getTargetLanguage())
                .challengeType(newChallenge.getChallengeType())
                .lessonType(lesson.getLessonType())
                .challengeStatus(status)
                .title(newChallenge.getTitle())
                .expiresAt(expiresAt)
                .build();

        switch (lesson.getLessonType()) {
            case QCM -> {
                List<QcmQuestionEntity> questions = new ArrayList<>(((QcmLesson) lesson).getQuestions());
                Collections.shuffle(questions);
                List<ChallengeQcm> qcm = questions.stream().limit(count).map(q ->
                    ChallengeQcm.builder()
                        .challenge(challenge)
                        .question(q.getQuestion())
                        .options(q.getOptions())
                        .correctOptionIndex(q.getCorrectOptionIndex())
                        .explanation(q.getExplanation())
                        .build()
                ).collect(Collectors.toList());
                challenge.setQcm(qcm);
            }
            case FLASHCARD -> {
                List<FlashcardEntity> flashcards = new ArrayList<>(((FlashcardLesson) lesson).getFlashcards());
                Collections.shuffle(flashcards);
                List<ChallengeFlashCard> challengeFlashCards = flashcards.stream().limit(count).map(f ->
                    ChallengeFlashCard.builder()
                        .challenge(challenge)
                        .front(f.getFront())
                        .back(f.getBack())
                        .frontLanguage(f.getFrontLanguage())
                        .backLanguage(f.getBackLanguage())
                        .timeLimitSeconds(30)
                        .build()
                ).collect(Collectors.toList());
                challenge.setFlashcards(challengeFlashCards);
            }
            case MATCHING_PAIR -> {
                List<MatchingPairEntity> pairs = new ArrayList<>(((MatchingPairLesson) lesson).getMatchingPairs());
                Collections.shuffle(pairs);
                List<ChallengeMatchingPair> challengePairs = pairs.stream().limit(count).map(p ->
                    ChallengeMatchingPair.builder()
                        .challenge(challenge)
                        .item1(p.getItem1())
                        .item2(p.getItem2())
                        .build()
                ).collect(Collectors.toList());
                challenge.setMatchingPairs(challengePairs);
            }
            case SORTING_EXERCISE -> {
                List<SortingExerciseEntity> exercises = new ArrayList<>(((SortingExerciseLesson) lesson).getSortingExercise());
                Collections.shuffle(exercises);
                List<ChallengeSortingExercise> challengeExercises = exercises.stream().limit(count).map(e ->
                    ChallengeSortingExercise.builder()
                        .challenge(challenge)
                        .items(e.getItems())
                        .correctOrder(e.getCorrectOrder())
                        .build()
                ).collect(Collectors.toList());
                challenge.setSortingExercises(challengeExercises);
            }
        }

        return challenge;
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
                .challenger(toUserResponse(challenge.getChallenger(), challengerProfile))
                .challenged(challenge.getChallenged() != null ? toUserResponse(challenge.getChallenged(), challengedProfile) : null)
                .qcm(challenge.getQcm().stream().map(this::toQcmResponse).collect(Collectors.toList()))
                .flashcards(challenge.getFlashcards().stream().map(this::toFlashCardResponse).collect(Collectors.toList()))
                .matchingPairs(challenge.getMatchingPairs().stream().map(this::toMatchingPairResponse).collect(Collectors.toList()))
                .sortingExercises(challenge.getSortingExercises().stream().map(this::toSortingExerciseResponse).collect(Collectors.toList()))
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
}
