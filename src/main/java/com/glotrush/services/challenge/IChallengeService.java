package com.glotrush.services.challenge;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.request.challenge.ChallengeResponseRequest;
import com.glotrush.dto.request.challenge.CreateChallengeRequest;
import com.glotrush.dto.response.challenge.ChallengeResponse;
import com.glotrush.dto.response.challenge.ChallengeUserResponse;
import com.glotrush.entities.challenge.Challenge;

public interface IChallengeService {

    ChallengeResponse createChallenge(UUID accountId, CreateChallengeRequest newChallenge);
    ChallengeResponse getChallenge(UUID challengeId, UUID accountId);
    List<ChallengeResponse> getUserChallenge(UUID accountId);
    List<ChallengeResponse> getPublicChallenges();
    ChallengeResponse acceptChallenge(UUID challengeId, UUID accountId);
    ChallengeResponse submitChallenge(UUID challengeId, UUID accountId, ChallengeResponseRequest response);
    ChallengeResponse declineChallenge(UUID challengeId, UUID accountId);
    ChallengeResponse joinChallenge(UUID challengeId, UUID accountId);
    void giveResultWhenExpired(Challenge challenge);
    ChallengeResponse cancelChallenge(UUID challengeId, UUID accountId);
    List<ChallengeUserResponse> getChallengeUsers(UUID languageId, UUID accountId);

}
