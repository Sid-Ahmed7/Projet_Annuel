package com.glotrush.services.challenge;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.glotrush.exceptions.UserNotFoundException;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.glotrush.builder.ChallengeBuilder;
import com.glotrush.constants.ChallengeConstants;
import com.glotrush.dto.request.challenge.ChallengeResponseRequest;
import com.glotrush.dto.request.challenge.CreateChallengeRequest;
import com.glotrush.dto.response.challenge.ChallengeProgress;
import com.glotrush.dto.response.challenge.ChallengeResponse;
import com.glotrush.dto.response.challenge.ChallengeUserResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Language;
import com.glotrush.entities.UserProfile;
import com.glotrush.entities.challenge.Challenge;
import com.glotrush.entities.challenge.ChallengeParticipant;
import com.glotrush.enumerations.ChallengeStatus;
import com.glotrush.enumerations.ChallengeType;
import com.glotrush.enumerations.LessonType;
import com.glotrush.exceptions.ChallengeAccessDeniedException;
import com.glotrush.exceptions.ChallengeCannotAcceptException;
import com.glotrush.exceptions.ChallengeExpiredException;
import com.glotrush.exceptions.ChallengeNotActiveException;
import com.glotrush.exceptions.ChallengeNotFoundException;
import com.glotrush.exceptions.ChallengedUserNotFoundException;
import com.glotrush.exceptions.InvalidDuelTypeException;
import com.glotrush.dto.request.challenge.ChallengeNotificationRequest;
import com.glotrush.entities.Lesson;
import com.glotrush.exceptions.LessonNotFoundException;
import com.glotrush.exceptions.MissingChallengedIdException;
import com.glotrush.exceptions.ScoreAlreadySubmittedException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.ChallengeParticipantsRepository;
import com.glotrush.repositories.ChallengeRepository;
import com.glotrush.repositories.LanguageRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.UserLanguageRepository;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.repositories.UserProgressRepository;
import com.glotrush.enumerations.LanguageType;
import com.glotrush.services.progress.IProgressService;
import com.glotrush.utils.LocaleUtils;
import com.glotrush.websocket.challenge.IChallengeWsService;
import com.glotrush.websocket.challenge.ChallengeSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChallengeService implements IChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipantsRepository challengeParticipantRepository;
    private final AccountsRepository accountsRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProgressRepository userProgressRepository;
    private final IChallengeWsService challengeWsService;
    private final MessageSource messageSource;
    private final LessonRepository lessonRepository;
    private final LanguageRepository languageRepository;
    private final UserLanguageRepository userLanguageRepository;
    private final ChallengeBuilder challengeBuilder;
    private final IProgressService progressService;


    @Override
    @Transactional
    public ChallengeResponse createChallenge(UUID accountId, CreateChallengeRequest newChallenge) {
        Accounts challenger = accountsRepository.findById(accountId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, LocaleUtils.getCurrentLocale())));
        Lesson lesson = null;
        Language language = null;
        Accounts challenged = null;
     
        if (newChallenge.getChallengeType() == ChallengeType.DUEL) {
            if (newChallenge.getLessonId() == null)
                throw new LessonNotFoundException(messageSource.getMessage("error.lesson.not_found", null, LocaleUtils.getCurrentLocale()));
            lesson = lessonRepository.findById(newChallenge.getLessonId()).orElseThrow(() -> new LessonNotFoundException(messageSource.getMessage("error.lesson.not_found", null, LocaleUtils.getCurrentLocale())));

            if (lesson.getLessonType() != LessonType.QCM && lesson.getLessonType() != LessonType.FLASHCARD)
                throw new InvalidDuelTypeException(messageSource.getMessage("error.invalid.duel.type", null, LocaleUtils.getCurrentLocale()));

            if (newChallenge.getChallengedId() == null)
                throw new MissingChallengedIdException(messageSource.getMessage("error.missing.id", null, LocaleUtils.getCurrentLocale()));

            challenged = accountsRepository.findById(newChallenge.getChallengedId()).orElseThrow(() -> new ChallengedUserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, LocaleUtils.getCurrentLocale())));

        } else {
            language = languageRepository.findById(newChallenge.getLanguageId()).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, LocaleUtils.getCurrentLocale())));
        }

        Language sourceLanguage = null;
        if (lesson != null && lesson.getTopic() != null) {
            sourceLanguage = lesson.getTopic().getSourceLanguage();
        } else if (newChallenge.getSourceLanguageId() != null) {
            sourceLanguage = languageRepository.findById(newChallenge.getSourceLanguageId()).orElse(null);
        }

        Challenge challenge = challengeBuilder.buildChallenge(newChallenge, lesson, language, sourceLanguage, challenger, challenged);
        challenge.getParticipants().add(challengeBuilder.buildParticipant(challenge, challenger));

        Challenge savedChallenge = challengeRepository.save(challenge);

        if (newChallenge.getChallengeType() == ChallengeType.DUEL && challenged != null) {
            UserProfile challengerProfile = userProfileRepository.findByAccount_Id(challenger.getId()).orElse(null);
            challengeWsService.sendNotificationToChallenged(challenged.getId(),
                ChallengeNotificationRequest.builder()
                    .challengeId(savedChallenge.getId())
                    .username(challenger.getUsername())
                    .photourl(challengerProfile != null ? challengerProfile.getPhotoUrl() : null)
                    .lessonTitle(lesson.getTitle())
                    .lessonType(lesson.getLessonType())
                    .expiresAt(savedChallenge.getExpiresAt())
                    .build()
            );
        }
        return mapToChallengeResponse(savedChallenge, accountId);
    }

    @Override
    public ChallengeResponse getChallenge(UUID challengeId, UUID accountId) {
        Challenge challenge = challengeRepository.findById(challengeId).orElseThrow(() -> new ChallengeNotFoundException(messageSource.getMessage("error.challenge.not_found", null, LocaleUtils.getCurrentLocale())));
        return mapToChallengeResponse(challenge, accountId);
    }

    @Override
    public List<ChallengeResponse> getUserChallenge(UUID accountId) {
        return challengeRepository.findAllByAccountId(accountId).stream().map(challengeUser -> mapToChallengeResponse(challengeUser, accountId)).collect(Collectors.toList());
    }

    @Override
    public List<ChallengeResponse> getPublicChallenges(UUID accountId) {
        UserProfile profile = userProfileRepository.findByAccount_Id(accountId).orElse(null);
        UUID activeLanguageId = (profile != null && profile.getActiveLanguage() != null) ? profile.getActiveLanguage().getId() : null;
        UUID nativeLanguageId = userLanguageRepository.findByAccount_IdAndLanguageType(accountId, LanguageType.NATIVE).stream().findFirst().map(ul -> ul.getLanguage().getId()).orElse(null);
        return challengeRepository.findPublicChallengesWithFilters(activeLanguageId, nativeLanguageId).stream().map(challenge -> mapToChallengeResponse(challenge, null)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChallengeResponse acceptChallenge(UUID challengeId, UUID accountId) {
        Challenge challenge = challengeRepository.findById(challengeId).orElseThrow(() -> new ChallengeNotFoundException(messageSource.getMessage("error.challenge.not_found", null, LocaleUtils.getCurrentLocale())));
        if(!challenge.getChallenged().getId().equals(accountId)) {
            throw new ChallengeAccessDeniedException(messageSource.getMessage("error.challenge.access_denied", null, LocaleUtils.getCurrentLocale()));
        }

        if(challenge.getChallengeStatus() != ChallengeStatus.PENDING) {
            throw new ChallengeCannotAcceptException(messageSource.getMessage("error.challenge.cannot_accept", null, LocaleUtils.getCurrentLocale()));
        }
        
        Accounts challenged = accountsRepository.findById(accountId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, LocaleUtils.getCurrentLocale())));

        challenge.setChallengeStatus(ChallengeStatus.ACTIVE);
        ChallengeParticipant challengedParticipant = challengeBuilder.buildParticipant(challenge, challenged);
        challenge.getParticipants().add(challengedParticipant);
        Challenge confirmChallenge = challengeRepository.save(challenge);
        TransactionSynchronizationManager.registerSynchronization(
            new ChallengeSynchronization(() -> challengeWsService.sendNotificationToStartChallenge(challengeId))
        );
        return mapToChallengeResponse(confirmChallenge, accountId);
    }

    @Override
    @Transactional
    public ChallengeResponse submitChallenge(UUID challengeId, UUID accountId, ChallengeResponseRequest response) {
        Challenge challenge = challengeRepository.findById(challengeId).orElseThrow(() -> new ChallengeNotFoundException(messageSource.getMessage("error.challenge.not_found", null, LocaleUtils.getCurrentLocale())));

        if(challenge.getChallengeStatus() != ChallengeStatus.ACTIVE) {
            throw new ChallengeNotActiveException(messageSource.getMessage("error.challenge.not_active", null, LocaleUtils.getCurrentLocale()));
        }

        if(challenge.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new ChallengeExpiredException(messageSource.getMessage("error.challenge.expired", null, LocaleUtils.getCurrentLocale()));
        }

        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndAccountId(challengeId, accountId).orElseThrow(() -> new ChallengeAccessDeniedException(messageSource.getMessage("error.challenge.access_denied", null, LocaleUtils.getCurrentLocale())));
    
        if(participant.getCompletedAt() != null) {
            throw new ScoreAlreadySubmittedException(messageSource.getMessage("error.challenge.cannot_submit", null, LocaleUtils.getCurrentLocale()));
        }
    
        participant.setScore(response.getScore());
        participant.setTimePassed(response.getTimePassed());
        participant.setCompletedAt(java.time.LocalDateTime.now());
        challengeParticipantRepository.save(participant);

        Accounts account = accountsRepository.findById(accountId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, LocaleUtils.getCurrentLocale())));
        UserProfile userProfile = userProfileRepository.findByAccount_Id(accountId).orElseThrow (() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, LocaleUtils.getCurrentLocale())));

        Integer totalQuestions = switch(challenge.getLessonType()) {
            case QCM -> challenge.getQcm().size();
            case FLASHCARD -> challenge.getFlashcards().size();
            case MATCHING_PAIR -> challenge.getMatchingPairs().size();
            case SORTING_EXERCISE -> challenge.getSortingExercises().size();
        };

        UUID fChallengeId = challenge.getId();
        ChallengeProgress progressToSend = ChallengeProgress.builder()
            .challengeId(fChallengeId)
            .accountId(accountId)
            .username(account.getUsername())
            .photoUrl(userProfile != null ? userProfile.getPhotoUrl() : null)
            .score(challenge.getChallengeType() == ChallengeType.DUEL ? null : response.getScore())
            .questionAnswered(totalQuestions)
            .timePassed(response.getTimePassed())
            .totalQuestions(totalQuestions)
            .build();
        TransactionSynchronizationManager.registerSynchronization(
            new ChallengeSynchronization(() -> challengeWsService.sendChallengeProgress(fChallengeId, progressToSend))
        );


        List<ChallengeParticipant> allUsers = challengeParticipantRepository.findByChallengeIdOrderByScoreDescTimePassedAsc(challengeId);
        boolean allCompleted = allUsers.stream().allMatch(user -> user.getCompletedAt() != null);
        
        if(allCompleted || challenge.getChallengeType() == ChallengeType.PUBLIC ) {
            if(challenge.getChallengeType() == ChallengeType.DUEL && allCompleted) {
                completeDuelChallenge(challenge, allUsers);
            } else {
                updatePublicChallengeRankings(challenge, allUsers, false);
            }
        }
        return mapToChallengeResponse(challengeRepository.save(challenge), accountId);
    
    }
    @Override
    @Transactional
    public ChallengeResponse declineChallenge(UUID challengeId, UUID accountId) {
    
        Challenge challenge = challengeRepository.findById(challengeId).orElseThrow(() -> new ChallengeNotFoundException(messageSource.getMessage("error.challenge.not_found", null, LocaleUtils.getCurrentLocale())));
        if(!challenge.getChallenged().getId().equals(accountId)) {
            throw new ChallengeAccessDeniedException(messageSource.getMessage("error.challenge.access_denied", null, LocaleUtils.getCurrentLocale()));
        }
        
        challenge.setChallengeStatus(ChallengeStatus.DECLINED);
        return mapToChallengeResponse(challengeRepository.save(challenge), accountId);
    }

    @Override
    @Transactional
    public ChallengeResponse joinChallenge(UUID challengeId, UUID accountId) {

        Challenge challenge = challengeRepository.findById(challengeId).orElseThrow(() -> new ChallengeNotFoundException(messageSource.getMessage("error.challenge.not_found", null, LocaleUtils.getCurrentLocale())));
        
        if(challenge.getChallengeStatus() != ChallengeStatus.ACTIVE) {
            throw new ChallengeNotActiveException(messageSource.getMessage("error.challenge.not_active", null, LocaleUtils.getCurrentLocale()));
        }

        if(challenge.getChallengeType() != ChallengeType.PUBLIC) {
            throw new ChallengeAccessDeniedException(messageSource.getMessage("error.challenge.access_denied", null, LocaleUtils.getCurrentLocale()));
        }

        if(challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ChallengeExpiredException(messageSource.getMessage("error.challenge.expired", null, LocaleUtils.getCurrentLocale()));
        }

        boolean isAlreadyJoined = challengeParticipantRepository.findByChallengeIdAndAccountId(challengeId, accountId).isPresent();
        if(isAlreadyJoined) {
            throw new ChallengeAccessDeniedException(messageSource.getMessage("error.challenge.already_joined", null, LocaleUtils.getCurrentLocale()));
        }

        Accounts account = accountsRepository.findById(accountId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, LocaleUtils.getCurrentLocale())));

        ChallengeParticipant participant = challengeBuilder.buildParticipant(challenge, account);
        challenge.getParticipants().add(participant);

        return mapToChallengeResponse(challengeRepository.save(challenge), accountId);
    }

    
    @Override
    @Transactional
    public void giveResultWhenExpired(Challenge challenge) {
        if(challenge.getChallengeType() == ChallengeType.DUEL) {
            List<ChallengeParticipant> participants = challenge.getParticipants().stream().filter(participant -> participant.getCompletedAt() != null).collect(Collectors.toList());
            if(participants.size() == 1) {
                ChallengeParticipant wParticiapnt = participants.get(0);
                wParticiapnt.setFinalRank(1);
                wParticiapnt.setXpGained((long) (ChallengeConstants.XP_WIN_CHALLENGE_DUEL));
                wParticiapnt.setScoreRecorded(true);
                addXp(wParticiapnt.getAccount().getId(), challenge.getLanguage().getId(), ChallengeConstants.XP_WIN_CHALLENGE_DUEL);
                challengeParticipantRepository.save(wParticiapnt);

                challenge.getParticipants().stream().filter(participant -> participant.getCompletedAt() == null).forEach(participant -> {
                    participant.setFinalRank(2);
                    participant.setXpGained((long) (ChallengeConstants.XP_LOSE_CHALLENGE_DUEL));
                    participant.setScoreRecorded(true);
                    addXp(participant.getAccount().getId(), challenge.getLanguage().getId(), ChallengeConstants.XP_LOSE_CHALLENGE_DUEL);
                    challengeParticipantRepository.save(participant);
                });
           }
        } else {
            updatePublicChallengeRankings(challenge, challenge.getParticipants(), true);
        }
    }

    @Override
    @Transactional
    public ChallengeResponse cancelChallenge(UUID challengeId, UUID accountId) {
        
        Challenge challenge = challengeRepository.findById(challengeId).orElseThrow(() -> new ChallengeNotFoundException(messageSource.getMessage("error.challenge.not_found", null, LocaleUtils.getCurrentLocale())));
        if(!challenge.getChallenger().getId().equals(accountId)) {
            throw new ChallengeAccessDeniedException(messageSource.getMessage("error.challenge.access_denied", null, LocaleUtils.getCurrentLocale()));
        }
        
        if(challenge.getChallengeStatus() != ChallengeStatus.PENDING) {
            throw new ChallengeCannotAcceptException(messageSource.getMessage("error.challenge.cannot_cancel", null, LocaleUtils.getCurrentLocale()));
        }

        challenge.setChallengeStatus(ChallengeStatus.CANCELLED);
        return mapToChallengeResponse(challengeRepository.save(challenge), accountId);
    }

    @Override
    public List<ChallengeUserResponse> getChallengeUsers(UUID languageId, UUID accountId) {
        return accountsRepository.findAccountsByLanguageId(languageId, accountId).stream().map(account -> {
                UserProfile userProfile = userProfileRepository.findByAccount_Id(account.getId()).orElse(null);
                return ChallengeUserResponse.builder()
                    .id(account.getId())
                    .username(account.getUsername())
                    .photoUrl(userProfile != null ? userProfile.getPhotoUrl() : null)
                    .build();
            }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void startChallenge(UUID challengeId, UUID accountId) {
        challengeParticipantRepository.findByChallengeIdAndAccountId(challengeId, accountId).orElseThrow(() -> new ChallengeAccessDeniedException(messageSource.getMessage("error.challenge.access_denied", null, LocaleUtils.getCurrentLocale())));
        Accounts account = accountsRepository.findById(accountId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.auth.account_not_found", null, LocaleUtils.getCurrentLocale())));
        UserProfile profile = userProfileRepository.findByAccount_Id(accountId).orElse(null);

        challengeWsService.sendChallengeProgress(challengeId,
            ChallengeProgress.builder()
                .challengeId(challengeId)
                .accountId(accountId)
                .username(account.getUsername())
                .photoUrl(profile != null ? profile.getPhotoUrl() : null)
                .build()
        );
    }

    private ChallengeResponse mapToChallengeResponse(Challenge challenge, UUID currentAccountId) {
        UserProfile challengerProfile = userProfileRepository.findByAccount_Id(challenge.getChallenger().getId()).orElse(null);
        UserProfile challengedProfile = challenge.getChallenged() != null ? userProfileRepository.findByAccount_Id(challenge.getChallenged().getId()).orElse(null) : null;

       Map<UUID, UserProfile> profiles = new HashMap<>();

       for (ChallengeParticipant participant : challenge.getParticipants()) {
            UUID accountId = participant.getAccount().getId();
            profiles.putIfAbsent(accountId,userProfileRepository.findByAccount_Id(accountId).orElse(null));
        }
        return challengeBuilder.toResponse(challenge, currentAccountId, challengerProfile, challengedProfile, profiles);
    }


    private void completeDuelChallenge(Challenge challenge, List<ChallengeParticipant> participants) {
       challenge.setChallengeStatus(ChallengeStatus.COMPLETED);

       sortResult(participants);
       for(int i = 0; i < participants.size(); i++) {
            ChallengeParticipant participant = participants.get(i);
            participant.setFinalRank(i + 1);
            participant.setScoreRecorded(true);
            participant.setXpGained((long) (i== 0 ? ChallengeConstants.XP_WIN_CHALLENGE_DUEL : ChallengeConstants.XP_LOSE_CHALLENGE_DUEL));
            addXp(participant.getAccount().getId(), challenge.getLanguage().getId(), participant.getXpGained().intValue());
            challengeParticipantRepository.save(participant);
       }
       UUID challengeId = challenge.getId();
       List<ChallengeProgress> results = participants.stream().map(participant -> {
            Accounts account = participant.getAccount();
            UserProfile prof = userProfileRepository.findByAccount_Id(account.getId()).orElse(null);
            return ChallengeProgress.builder()
                .challengeId(challengeId)
                .accountId(account.getId())
                .username(account.getUsername())
                .photoUrl(prof != null ? prof.getPhotoUrl() : null)
                .score(participant.getScore())
                .timePassed(participant.getTimePassed())
                .finalRank(participant.getFinalRank())
                .xpGained(participant.getXpGained() != null ? participant.getXpGained().intValue() : null)
                .build();
        }).collect(Collectors.toList());
       TransactionSynchronizationManager.registerSynchronization(
           new ChallengeSynchronization(() -> results.forEach(result -> challengeWsService.sendChallengeResult(challengeId, result)))
       );

    }

    private void sortResult(List<ChallengeParticipant> participants) {
       participants.sort(Comparator.comparingDouble(ChallengeParticipant::getScore).reversed().thenComparing(ChallengeParticipant::getTimePassed));
    }

    private void addXp(UUID accountId, UUID languageId, int xp) {
        userProgressRepository.findByAccount_IdAndTopic_TargetLanguage_Id(accountId, languageId).forEach(progress -> progressService.addXP(accountId, progress.getTopic().getId(), xp));
    }

    private void updatePublicChallengeRankings(Challenge challenge, List<ChallengeParticipant> participants, boolean giveXp) {
       List<ChallengeParticipant> rankedParticipants = participants.stream().filter(participant -> participant.getCompletedAt() != null).sorted(
        Comparator.comparingDouble(ChallengeParticipant::getScore).reversed().thenComparing(ChallengeParticipant::getTimePassed)
       ).collect(Collectors.toList());

       for(int i = 0; i < rankedParticipants.size(); i++) {
            ChallengeParticipant participant = rankedParticipants.get(i);
            participant.setFinalRank(i + 1);
            if(giveXp) {
                participant.setScoreRecorded(true);
                long xp = switch(i) {
                    case 0 -> ChallengeConstants.XP_WIN_FIRST_PLACE_CHALLENGE_PUBLIC;
                    case 1 -> ChallengeConstants.XP_WIN_SECOND_PLACE_CHALLENGE_PUBLIC;
                    case 2 -> ChallengeConstants.XP_WIN_THIRD_PLACE_CHALLENGE_PUBLIC;
                    default -> ChallengeConstants.XP_PARTICIPATION;
                };
                participant.setXpGained(xp);
            }
            challengeParticipantRepository.save(participant);
        }

       UUID challengeId = challenge.getId();
       List<ChallengeProgress> publicResults = rankedParticipants.stream().map(participant -> {
            Accounts account = participant.getAccount();
            UserProfile profile = userProfileRepository.findByAccount_Id(account.getId()).orElse(null);
            return ChallengeProgress.builder()
                .challengeId(challengeId)
                .accountId(account.getId())
                .username(account.getUsername())
                .photoUrl(profile != null ? profile.getPhotoUrl() : null)
                .score(participant.getScore())
                .timePassed(participant.getTimePassed())
                .finalRank(participant.getFinalRank())
                .xpGained(participant.getXpGained() != null ? participant.getXpGained().intValue() : null)
                .build();
        }).collect(Collectors.toList());
       TransactionSynchronizationManager.registerSynchronization(
           new ChallengeSynchronization(() -> publicResults.forEach(result -> challengeWsService.sendChallengeResult(challengeId, result)))
       );
    }


}
