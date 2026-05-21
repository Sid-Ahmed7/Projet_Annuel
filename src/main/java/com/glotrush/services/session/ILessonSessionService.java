package com.glotrush.services.session;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.request.LessonSessionRequest;
import com.glotrush.dto.response.LessonSessionResponse;

public interface ILessonSessionService {

    void saveLessonSession(LessonSessionRequest request);
    List<LessonSessionResponse> getAllSessionOfAnAccount(UUID accountId);
}