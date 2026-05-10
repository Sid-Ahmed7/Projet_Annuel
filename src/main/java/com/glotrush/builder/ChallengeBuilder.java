package com.glotrush.builder;

import java.time.LocalDateTime;
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
import com.glotrush.entities.Language;
import com.glotrush.entities.UserProfile;
import com.glotrush.entities.challenge.Challenge;
import com.glotrush.entities.challenge.ChallengeFlashCard;
import com.glotrush.entities.challenge.ChallengeMatchingPair;
import com.glotrush.entities.challenge.ChallengeParticipant;
import com.glotrush.entities.challenge.ChallengeQcm;
import com.glotrush.entities.challenge.ChallengeSortingExercise;
import com.glotrush.enumerations.ChallengeStatus;
import com.glotrush.enumerations.ChallengeType;

public class ChallengeBuilder {


    public Challenge buildChallenge(CreateChallengeRequest newChallenge, Accounts challenger,Accounts challenged, Language language) {
        ChallengeStatus status = newChallenge.getChallengeType() == ChallengeType.PUBLIC ? ChallengeStatus.ACTIVE : ChallengeStatus.PENDING;

        Challenge challenge = Challenge.builder()
                .challenger(challenger)
                .challenged(challenged)
                .language(language)
                .challengeType(newChallenge.getChallengeType())
                .lessonType(newChallenge.getLessonType())
                .challengeStatus(status)
                .title(newChallenge.getTitle())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        if (newChallenge.getQcm() != null) {
            List<ChallengeQcm> qcm = newChallenge.getQcm().stream().map(q ->
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

        if (newChallenge.getFlashcards() != null) {
            List<ChallengeFlashCard> flashcards = newChallenge.getFlashcards().stream().map(flashCard ->
                ChallengeFlashCard.builder()
                    .challenge(challenge)
                    .front(flashCard.getFront())
                    .back(flashCard.getBack())
                    .frontLanguage(flashCard.getFrontLanguage())
                    .backLanguage(flashCard.getBackLanguage())
                    .timeLimitSeconds(flashCard.getTimeLimitSeconds() != null ? flashCard.getTimeLimitSeconds() : 30)
                    .build()
            ).collect(Collectors.toList());
            challenge.setFlashcards(flashcards);
        }

        if (newChallenge.getMatchingPairs() != null) {
            List<ChallengeMatchingPair> pairs = newChallenge.getMatchingPairs().stream().map(pair ->
                ChallengeMatchingPair.builder()
                    .challenge(challenge)
                    .item1(pair.getItem1())
                    .item2(pair.getItem2())
                    .build()
            ).collect(Collectors.toList());
            challenge.setMatchingPairs(pairs);
        }

        if (newChallenge.getSortingExercises() != null) {
            List<ChallengeSortingExercise> exercises = newChallenge.getSortingExercises().stream().map(sortingExercise ->
                ChallengeSortingExercise.builder()
                    .challenge(challenge)
                    .items(sortingExercise.getItems())
                    .correctOrder(sortingExercise.getCorrectOrder())
                    .build()
            ).collect(Collectors.toList());
            challenge.setSortingExercises(exercises);
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
