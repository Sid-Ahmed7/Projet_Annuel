package com.glotrush.builder;

import org.springframework.stereotype.Component;

import com.glotrush.dto.response.answers.UserMistakeListAnswerResponse;
import com.glotrush.dto.response.answers.UserMistakeRetryListResponse;
import com.glotrush.dto.response.answers.UserMistakeRetryListResponse.UserMistakeRetryListResponseBuilder;
import com.glotrush.entities.UserMistake;

@Component
public class UserMistakeBuilder {

    public UserMistakeRetryListResponseBuilder retryListResponse(UserMistake mistake, String nextReviewIndication) {
        return UserMistakeRetryListResponse.builder()
            .userMistakeId(mistake.getId())
            .lessonType(mistake.getLessonType())
            .learningType(mistake.getLearningType())
            .topicName(mistake.getTopic() != null ? mistake.getTopic().getName() : null)
            .nextReviewIndication(nextReviewIndication)
            .nbCorrectResponses(mistake.getConsecutiveGoodAnswers())
            .userAnswer(mistake.getUserAnswer())
            .createdAt(mistake.getCreatedAt());
    }

    public UserMistakeListAnswerResponse.UserMistakeListAnswerResponseBuilder listAnswerResponse(UserMistake mistake, String nextReviewIn) {
        return UserMistakeListAnswerResponse.builder()
            .mistakeItemId(mistake.getId())
            .lessonType(mistake.getLessonType())
            .learningType(mistake.getLearningType())
            .topicName(mistake.getTopic() != null ? mistake.getTopic().getName() : null)
            .addedAt(mistake.getCreatedAt())
            .nextReviewIn(nextReviewIn)
            .consecutiveCorrect(mistake.getConsecutiveGoodAnswers())
            .userWrongAnswer(mistake.getUserAnswer());
    }
}
