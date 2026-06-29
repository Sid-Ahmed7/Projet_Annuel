package com.glotrush.dto.response.challenge;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.glotrush.enumerations.ChallengeStatus;
import com.glotrush.enumerations.ChallengeType;
import com.glotrush.enumerations.LessonType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeResponse {

    private UUID id;
    private String title;
    private ChallengeType challengeType;
    private LessonType lessonType;
    private ChallengeStatus challengeStatus;
    private UUID languageId;
    private UUID sourceLanguageId;
    private String sourceLanguageCode;
    private String sourceLanguageName;
    private ChallengeUserResponse challenger;
    private ChallengeUserResponse challenged;
    private List<ChallengeQcmResponse> qcm;
    private List<ChallengeFlashCardResponse> flashcards;
    private List<ChallengeMatchingPairResponse> matchingPairs;
    private List<ChallengeSortingExerciseResponse> sortingExercises;
    private List<ChallengeParticipantResponse> participants;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
