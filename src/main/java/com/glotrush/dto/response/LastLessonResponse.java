package com.glotrush.dto.response;

import java.util.UUID;

import com.glotrush.enumerations.LessonType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor
public class LastLessonResponse {
    private UUID lessonId;
    private String lessonName;
    private UUID topicId;
    private LessonType lessonType;
    private Integer xpReward;
    private String topicName;
    private String languageName;
    private String languageCode;
    private Integer completionCount;
    private Integer totalLessonsInTopic;
    
}
