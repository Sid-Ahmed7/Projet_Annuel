package com.glotrush.dto.response.answers;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerResponse {

    private UUID userMistakeId;
    private boolean isAnswerCorrect;
    private Integer nbCorrectResponses;
    private boolean isLessonMastered;
    private String nextReviewIndication;
    
}
