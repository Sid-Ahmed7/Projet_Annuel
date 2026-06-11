package com.glotrush.dto.response.answers;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResultResponse {
    private Integer totalAnswers;
    private Integer totalCorrectAnswers;
    private Integer totalMasteredLessons;
    private List<UserAnswerResponse> answerResults;
    
}
