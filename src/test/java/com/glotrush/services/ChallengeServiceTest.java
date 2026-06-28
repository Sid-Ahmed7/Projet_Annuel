package com.glotrush.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.MockedStatic;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.glotrush.builder.ChallengeBuilder;
import com.glotrush.config.TestMessageSourceConfig;
import com.glotrush.dto.request.challenge.CreateChallengeRequest;
import com.glotrush.dto.response.challenge.ChallengeResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.lesson.InteractiveLesson;
import com.glotrush.entities.challenge.Challenge;
import com.glotrush.entities.challenge.ChallengeParticipant;
import com.glotrush.enumerations.ChallengeStatus;
import com.glotrush.enumerations.ChallengeType;
import com.glotrush.enumerations.LessonType;
import com.glotrush.exceptions.LessonNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.ChallengeParticipantsRepository;
import com.glotrush.repositories.ChallengeRepository;
import com.glotrush.repositories.LanguageRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.repositories.UserProgressRepository;
import com.glotrush.services.challenge.ChallengeService;
import com.glotrush.services.challenge.IChallengeService;
import com.glotrush.services.progress.IProgressService;
import com.glotrush.websocket.challenge.IChallengeWsService;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(classes = TestMessageSourceConfig.class)
@DisplayName("ChallengeService Unit Tests")
public class ChallengeServiceTest {

    @Autowired
    private MessageSource messageSource;

    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private ChallengeParticipantsRepository challengeParticipantsRepository;
    @Mock
    private AccountsRepository accountsRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private UserProgressRepository userProgressRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private LanguageRepository languageRepository;
    @Mock
    private IProgressService progressService;
    @Mock
    private IChallengeWsService challengeWsService;
    @Mock
    private ChallengeBuilder challengeBuilder;

    private IChallengeService challengeService;

    private UUID challengerId;
    private UUID challengedId;
    private UUID challengeId;
    private UUID lessonId;
    private UUID languageId;
    private Accounts challenger;
    private Accounts challenged;
    private Challenge challenge;
    private Lesson lesson;
    
    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        challengeService = new ChallengeService(challengeRepository, challengeParticipantsRepository, accountsRepository, userProfileRepository, userProgressRepository, challengeWsService, messageSource, lessonRepository, languageRepository, challengeBuilder, progressService);

        challengerId = UUID.randomUUID();
        challengedId = UUID.randomUUID();
        challengeId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
        languageId = UUID.randomUUID();

        challenger = Accounts.builder().id(challengerId).username("Gojo").build();
        challenged = Accounts.builder().id(challengedId).username("Sukuna").build();

        lesson = mock(Lesson.class);

        challenge = mock(Challenge.class);
        lenient().when(challenge.getChallenger()).thenReturn(challenger);
        lenient().when(challenge.getChallenged()).thenReturn(challenged);
        lenient().when(challenge.getParticipants()).thenReturn(new ArrayList<>());
    }
    
    @Test
    @DisplayName("Should create DUEL challenge successfully")
    void createChallenge_duelSuccess() {
        when(lesson.getLessonType()).thenReturn(LessonType.QCM);
        when(lesson.getTitle()).thenReturn("Test Lesson");
        when(challenge.getId()).thenReturn(challengeId);
        when(challenge.getExpiresAt()).thenReturn(LocalDateTime.now().plusHours(1));

        CreateChallengeRequest request = new CreateChallengeRequest();
        request.setChallengeType(ChallengeType.DUEL);
        request.setLessonId(lessonId);
        request.setChallengedId(challengedId);

        ChallengeParticipant mockParticipant = mock(ChallengeParticipant.class);
        when(mockParticipant.getAccount()).thenReturn(challenger);

        when(accountsRepository.findById(challengerId)).thenReturn(Optional.of(challenger));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(accountsRepository.findById(challengedId)).thenReturn(Optional.of(challenged));
        when(challengeBuilder.buildChallenge(any(), any(), any(), any(), any())).thenReturn(challenge);
        when(challengeBuilder.buildParticipant(any(), any())).thenReturn(mockParticipant);
        when(challengeRepository.save(any())).thenReturn(challenge);
        when(userProfileRepository.findByAccount_Id(any())).thenReturn(Optional.empty());
        when(challengeBuilder.toResponse(any(), any(), any(), any(), any())).thenReturn(ChallengeResponse.builder().build());

        ChallengeResponse result = challengeService.createChallenge(challengerId, request);

        assertThat(result).isNotNull();
        verify(challengeRepository).save(any());
    }
    
    @Test
    @DisplayName("Should create DUEL challenge successfully with interactive lesson")
    void createChallenge_duelInteractiveSuccess() {
        InteractiveLesson interactiveLesson = mock(InteractiveLesson.class);
        when(interactiveLesson.getLessonType()).thenReturn(LessonType.INTERACTIVE);
        when(interactiveLesson.getTitle()).thenReturn("Interactive Lesson");
        when(challenge.getId()).thenReturn(challengeId);
        when(challenge.getExpiresAt()).thenReturn(LocalDateTime.now().plusHours(1));

        CreateChallengeRequest request = new CreateChallengeRequest();
        request.setChallengeType(ChallengeType.DUEL);
        request.setLessonId(lessonId);
        request.setChallengedId(challengedId);

        ChallengeParticipant mockParticipant = mock(ChallengeParticipant.class);
        when(mockParticipant.getAccount()).thenReturn(challenger);

        when(accountsRepository.findById(challengerId)).thenReturn(Optional.of(challenger));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(interactiveLesson));
        when(accountsRepository.findById(challengedId)).thenReturn(Optional.of(challenged));
        when(challengeBuilder.buildChallenge(any(), any(), any(), any(), any())).thenReturn(challenge);
        when(challengeBuilder.buildParticipant(any(), any())).thenReturn(mockParticipant);
        when(challengeRepository.save(any())).thenReturn(challenge);
        when(userProfileRepository.findByAccount_Id(any())).thenReturn(Optional.empty());
        when(challengeBuilder.toResponse(any(), any(), any(), any(), any())).thenReturn(ChallengeResponse.builder().build());

        ChallengeResponse result = challengeService.createChallenge(challengerId, request);

        assertThat(result).isNotNull();
        verify(challengeRepository).save(any());
    }
    
    @Test
    @DisplayName("Should throw LessonNotFoundException for DUEL")
    void createChallenge_duelWithoutLesson() {
        CreateChallengeRequest request = new CreateChallengeRequest();
        request.setChallengeType(ChallengeType.DUEL);
        request.setLessonId(null);
        when(accountsRepository.findById(challengerId)).thenReturn(Optional.of(challenger));

        assertThatThrownBy(() -> challengeService.createChallenge(challengerId, request))
            .isInstanceOf(LessonNotFoundException.class);
    }
    @Test
    @DisplayName("Should return challenge by id")
    void getChallenge_success() {
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(userProfileRepository.findByAccount_Id(any())).thenReturn(Optional.empty());
        when(challengeBuilder.toResponse(any(), any(), any(), any(), any())).thenReturn(ChallengeResponse.builder().build());

        ChallengeResponse result = challengeService.getChallenge(challengeId, challengerId);

        assertThat(result).isNotNull();
    }
    
    @Test
    @DisplayName("Should accept challenge successfully")
    void acceptChallenge_success() {
        when(challenge.getChallengeStatus()).thenReturn(ChallengeStatus.PENDING);

        ChallengeParticipant mockParticipant = mock(ChallengeParticipant.class);
        when(mockParticipant.getAccount()).thenReturn(challenged);

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(accountsRepository.findById(challengedId)).thenReturn(Optional.of(challenged));
        when(challengeBuilder.buildParticipant(any(), any())).thenReturn(mockParticipant);
        when(challengeRepository.save(any())).thenReturn(challenge);
        when(userProfileRepository.findByAccount_Id(any())).thenReturn(Optional.empty());
        when(challengeBuilder.toResponse(any(), any(), any(), any(), any())).thenReturn(ChallengeResponse.builder().build());

        try (MockedStatic<TransactionSynchronizationManager> tsm = mockStatic(TransactionSynchronizationManager.class)) {
            tsm.when(() -> TransactionSynchronizationManager.registerSynchronization(any())).thenAnswer(inv -> null);

            ChallengeResponse result = challengeService.acceptChallenge(challengeId, challengedId);

            assertThat(result).isNotNull();
            verify(challengeRepository).save(any());
        }
    }
}
