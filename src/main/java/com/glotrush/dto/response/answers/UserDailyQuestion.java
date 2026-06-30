package com.glotrush.dto.response.answers;
import java.util.List;

import com.glotrush.dto.response.MistakeQuestionResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDailyQuestion {
    private boolean hasQuestionToday;
    private long totalQuestions;
    private long totalAvailableNow;
    private List<MistakeQuestionResponse> mistakeQuestions;
    
}

