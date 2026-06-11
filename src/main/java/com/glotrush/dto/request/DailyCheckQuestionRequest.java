package com.glotrush.dto.request;

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
public class DailyCheckQuestionRequest {

    private boolean hasQuestionToday;
    private long totalQuestions;
    private List<MistakeQuestionResponse> mistakeQuestions;
    
}
