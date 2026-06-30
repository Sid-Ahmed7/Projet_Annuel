package com.glotrush.dto.request.answer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerRequest {

    private UUID userMistakeId;
    private String translateAnswer;
    private Integer selectedResponseIndex;
    private String item2;
    private List<Integer> userOrderedResponseIndexes;
    
}
