package com.glotrush.dto.response.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressExportResponse {
    private String topic;
    private Long totalXP;
    private Integer completedLessons;
    private Double accuracy;
    private Boolean examPassed;  
}
