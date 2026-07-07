package com.glotrush.dto.request.ai;

import com.glotrush.constants.AILessonConstants;
import com.glotrush.enumerations.LessonType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AIChallengeGenerateRequest {

    @NotNull
    private LessonType lessonType;
    
    @NotNull
    private UUID sourceLanguageId;

    @NotNull
    private UUID targetLanguageId;
    
    @NotBlank
    private String description;

    @NotNull(message = "{error.ai.lesson.itemcount.required}")
    private Integer itemCount;

    @AssertTrue(message = "{error.ai.lesson.itemcount.invalid}")
    public boolean isValidItemCount() {
        if (itemCount == null) {
            return false;
        }
        
        if (lessonType == null) {
            return true;
        }
        
        if (lessonType == LessonType.FLASHCARD || lessonType == LessonType.QCM) {
            return itemCount >= AILessonConstants.MIN_ITEMS_FLASHCARD_QCM && itemCount <= AILessonConstants.MAX_ITEMS_FLASHCARD_QCM;
        }
        
        if (lessonType == LessonType.MATCHING_PAIR || lessonType == LessonType.SORTING_EXERCISE || lessonType == LessonType.INTERACTIVE) {
            return itemCount >= AILessonConstants.MIN_ITEMS_MATCHING_SORTING && itemCount <= AILessonConstants.MAX_ITEMS_MATCHING_SORTING;
        }
        
        return false;
    }
}
