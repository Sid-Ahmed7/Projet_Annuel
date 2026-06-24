package com.glotrush.dto.request.ai;

import com.glotrush.constants.AILessonConstants;
import com.glotrush.dto.request.LessonRequest;
import com.glotrush.enumerations.LessonType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AILessonModifyRequest {
    @NotNull
    private UUID lessonId;
    
    @NotBlank
    private String prompt;

    private Integer itemCount;

    @NotNull
    @Valid
    private LessonRequest lesson;

    @AssertTrue(message = "{error.ai.lesson.itemcount.invalid}")
    public boolean isValidItemCount() {
        if (itemCount == null) {
            return true;
        }
        
        if (lesson == null || lesson.getLessonType() == null) {
            return true;
        }
        
        LessonType type = lesson.getLessonType();
        if (type == LessonType.FLASHCARD || type == LessonType.QCM) {
            return itemCount >= AILessonConstants.MIN_ITEMS_FLASHCARD_QCM && itemCount <= AILessonConstants.MAX_ITEMS_FLASHCARD_QCM;
        }
        
        if (type == LessonType.MATCHING_PAIR || type == LessonType.SORTING_EXERCISE) {
            return itemCount >= AILessonConstants.MIN_ITEMS_MATCHING_SORTING && itemCount <= AILessonConstants.MAX_ITEMS_MATCHING_SORTING;
        }
        
        return false;
    }
}
