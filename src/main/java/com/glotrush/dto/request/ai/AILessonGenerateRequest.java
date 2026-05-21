package com.glotrush.dto.request.ai;

import com.glotrush.enumerations.LessonType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AILessonGenerateRequest {
    @NotNull
    private LessonType lessonType;
    
    @NotNull
    private UUID topicId;
    
    @NotBlank
    private String description;

    private Integer itemCount;

    @AssertTrue(message = "L'itemCount doit être compris entre 5 et 20 pour FLASHCARD et QCM, et entre 3 et 10 pour MATCHING_PAIR et SORTING_EXERCISE")
    public boolean isValidItemCount() {
        if (itemCount == null) {
            return true;
        }
        if (lessonType == null) {
            return true;
        }
        
        if (lessonType == LessonType.FLASHCARD || lessonType == LessonType.QCM) {
            return itemCount >= 5 && itemCount <= 20;
        }
        
        if (lessonType == LessonType.MATCHING_PAIR || lessonType == LessonType.SORTING_EXERCISE) {
            return itemCount >= 3 && itemCount <= 10;
        }
        
        return false;
    }
}
