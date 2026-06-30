package com.glotrush.scheduler.jobs.challenge;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.glotrush.entities.challenge.Challenge;
import com.glotrush.enumerations.ChallengeStatus;
import com.glotrush.repositories.ChallengeRepository;
import com.glotrush.services.challenge.IChallengeService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChallengeExpirationJob {

    private final ChallengeRepository challengeRepository;
    private final IChallengeService challengeService;


    @Scheduled(fixedRate = 30000)
    @Transactional
    public void checkExpiredChallenges() {
        List<Challenge> expiredChallenges = challengeRepository.findExpiredChallenges(LocalDateTime.now());
        for (Challenge challenge : expiredChallenges) {
            challenge.setChallengeStatus(ChallengeStatus.EXPIRED);
            challengeRepository.save(challenge);
            challengeService.giveResultWhenExpired(challenge);
        }
    }
}
