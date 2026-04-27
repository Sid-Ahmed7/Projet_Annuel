package com.glotrush.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.glotrush.dto.request.lesson.FlashcardLessonRequest;
import com.glotrush.dto.request.lesson.MatchingPairLessonRequest;
import com.glotrush.dto.request.lesson.QcmLessonRequest;
import com.glotrush.dto.request.lesson.SortingExerciseLessonRequest;
import com.glotrush.enumerations.LessonType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "lessonType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FlashcardLessonRequest.class, name = "FLASHCARD"),
        @JsonSubTypes.Type(value = MatchingPairLessonRequest.class, name = "MATCHING_PAIR"),
        @JsonSubTypes.Type(value = QcmLessonRequest.class, name = "QCM"),
        @JsonSubTypes.Type(value = SortingExerciseLessonRequest.class, name = "SORTING_EXERCISE")
})
@Data
public abstract class LessonRequest {
    @NotNull
    private UUID topicId;
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotNull
    private Boolean isActive;
    @NotNull
    private LessonType lessonType;
}
