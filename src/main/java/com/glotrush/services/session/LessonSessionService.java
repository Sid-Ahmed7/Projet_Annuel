package com.glotrush.services.session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.glotrush.dto.request.LessonSessionRequest;
import com.glotrush.dto.response.LessonSessionResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.LessonSession;
import com.glotrush.exceptions.LessonNotFoundException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.LessonRepository;
import com.glotrush.repositories.LessonSessionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LessonSessionService implements ILessonSessionService {

    private final LessonSessionRepository lessonSessionRepository;
    private final AccountsRepository accountsRepository;
    private final LessonRepository lessonRepository;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public void saveLessonSession(LessonSessionRequest request) {

        Accounts account = accountsRepository.findById(request.getAccountId()).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("account.not.found", null, null)));
        Lesson lesson = lessonRepository.findById(request.getLessonId()).orElseThrow(() -> new LessonNotFoundException(messageSource.getMessage("lesson.not.found", null, null)));

        LessonSession session = LessonSession.builder()
                .account(account)
                .lesson(lesson)
                .correctAnswers(request.getCorrectAnswers())
                .wrongAnswers(request.getTotalQuestions() - request.getCorrectAnswers())
                .totalQuestions(request.getTotalQuestions())
                .totalTime(request.getTotalTime())
                .status(request.getStatus())
                .completedAt(LocalDateTime.now())
                .build();

        lessonSessionRepository.save(session);
    }

    @Override
    public List<LessonSessionResponse> getAllSessionOfAnAccount(UUID accountId) {
        List<LessonSession> sessions = lessonSessionRepository.findByAccount_IdOrderByCompletedAtDesc(accountId);
        return sessions.stream().map(this::toResponse).toList();
    }


     private LessonSessionResponse toResponse(LessonSession session) {
        Lesson lesson = session.getLesson();
        return LessonSessionResponse.builder()
                .id(session.getId())
                .lessonId(lesson.getId())
                .lessonTitle(lesson.getTitle())
                .topicName(lesson.getTopic().getName())
                .lessonType(lesson.getLessonType())
                .correctAnswers(session.getCorrectAnswers())
                .wrongAnswers(session.getWrongAnswers())
                .totalQuestions(session.getTotalQuestions())
                .totalTime(session.getTotalTime())
                .status(session.getStatus())
                .completedAt(session.getCompletedAt())
                .build();
    }

    
}
