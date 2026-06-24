package com.glotrush.services.rewiewMistake;

import java.util.UUID;

import com.glotrush.dto.request.answer.UserAnswerResultRequest;
import com.glotrush.dto.request.answer.UserMistakeAddMultipleRequest;
import com.glotrush.dto.request.answer.UserMistakeAddRequest;
import com.glotrush.dto.response.answers.UserDailyQuestion;
import com.glotrush.dto.response.answers.UserMistakeListResponse;
import com.glotrush.dto.response.answers.UserMistakeRetryListResponse;
import com.glotrush.dto.response.answers.UserResultResponse;


public interface IReviewMistakeService {
    UserDailyQuestion getDailyMistakeQuestion(UUID accountId);
    UserResultResponse submitAnswer(UUID accountId, UserAnswerResultRequest answerResult);
    UserMistakeListResponse getPendingMistakesQuestion(UUID accountId);
    UserMistakeRetryListResponse getMistakeQuestionDetail(UUID accountId, UUID userMistakeId);
    void addToMistakeList(UUID accountId, UserMistakeAddRequest request);
    void addMultipleToMistakeList(UUID accountId, UserMistakeAddMultipleRequest request);

}
