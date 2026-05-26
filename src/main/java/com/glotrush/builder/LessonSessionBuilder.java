package com.glotrush.builder;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.glotrush.dto.request.CompleteLessonRequest;
import com.glotrush.dto.request.LessonSessionRequest;
import com.glotrush.enumerations.LessonSessionStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LessonSessionBuilder {

    public LessonSessionRequest buildLessonSessionRequest(UUID accountId, UUID lessonId, CompleteLessonRequest lessonRequest, LessonSessionStatus status) {
        return LessonSessionRequest.builder()
                .accountId(accountId)
                .lessonId(lessonId)
                .correctAnswers(lessonRequest.getCorrectAnswers() != null ? lessonRequest.getCorrectAnswers() : 0)
                .wrongAnswers(lessonRequest.getTotalAnswers() != null && lessonRequest.getCorrectAnswers() != null ? lessonRequest.getTotalAnswers() - lessonRequest.getCorrectAnswers() : 0)
                .totalQuestions(lessonRequest.getTotalAnswers() != null ? lessonRequest.getTotalAnswers() : 0)
                .totalTime(lessonRequest.getTimeSpentSeconds() != null ? lessonRequest.getTimeSpentSeconds() : 0)
                .status(status)
                .build();
    }

}
