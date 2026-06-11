package com.glotrush.dto.response.answers;

import java.util.UUID;

public class UserAnswerResponse {

    private UUID userMistakeId;
    private boolean isAnswerCorrect;
    private Integer nbCorrectResponses;
    private boolean isLessonMastered;
    private String nextReviewIndication;
    
}
