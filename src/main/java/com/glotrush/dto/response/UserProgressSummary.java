package com.glotrush.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressSummary {
   private Integer level;
    private Long totalXP;
    private Integer completedLessons;
    private Double completionPercentage;
    private Double accuracy;
}
