package com.glotrush.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteExamResponse {
    private Boolean success;
    private String message;
    private Integer xpEarned;
    private Long totalXP;
    private Integer currentLevel;
    private Boolean leveledUp;
    private Integer newLevel;
    private Integer totalAnswers;
    private Integer correctAnswers;
    private Double accuracy;
}
