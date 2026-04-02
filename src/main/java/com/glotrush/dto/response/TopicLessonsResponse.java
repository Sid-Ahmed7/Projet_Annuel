package com.glotrush.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicLessonsResponse {
    private String topicTitle;
    private List<LessonSummaryResponse> lessons;
    private Boolean examPassed;
    private Integer examAttempts;
    private Double lastAccuracy;
}
