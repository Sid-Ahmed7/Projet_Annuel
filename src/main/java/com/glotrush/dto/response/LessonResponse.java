package com.glotrush.dto.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.glotrush.dto.response.lesson.FlashcardLessonResponse;
import com.glotrush.dto.response.lesson.MatchingPairLessonResponse;
import com.glotrush.dto.response.lesson.QcmLessonResponse;
import com.glotrush.dto.response.lesson.SortingExerciseLessonResponse;
import com.glotrush.enumerations.LessonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FlashcardLessonResponse.class, name = "FLASHCARD"),
        @JsonSubTypes.Type(value = MatchingPairLessonResponse.class, name = "MATCHING_PAIR"),
        @JsonSubTypes.Type(value = QcmLessonResponse.class, name = "QCM"),
        @JsonSubTypes.Type(value = SortingExerciseLessonResponse.class, name = "SORTING_EXERCISE")
})
public class LessonResponse {
    private UUID id;
    private UUID topicId;
    private String topicName;
    private String title;
    private String description;
    private Integer orderIndex;
    private Integer xpReward;
    private Integer minLevelRequired;
    private Integer durationMinutes;
    private Integer passScorePercentage;
    private Boolean isActive;
    private LessonType lessonType;

    private UserLessonProgressSummary userProgress;
}
