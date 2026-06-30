package com.glotrush.dto.request.answer;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerResultRequest {
    
    private List<UserAnswerRequest> userAnswers;
}
