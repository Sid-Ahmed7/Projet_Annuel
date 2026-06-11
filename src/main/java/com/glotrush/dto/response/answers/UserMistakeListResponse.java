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
public class UserMistakeListResponse {
    private long totalPendingQuestions;
    private List<UserMistakeRetryListResponse> mistakes;
}
