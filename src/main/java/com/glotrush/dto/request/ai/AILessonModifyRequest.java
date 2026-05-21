package com.glotrush.dto.request.ai;

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

    @AssertTrue(message = "L'itemCount doit être compris entre 5 et 20 pour FLASHCARD et QCM, et entre 3 et 10 pour MATCHING_PAIR et SORTING_EXERCISE")
    public boolean isValidItemCount() {
        if (itemCount == null) {
            return true;
        }
        if (lesson == null || lesson.getLessonType() == null) {
            return true;
        }
        
        LessonType type = lesson.getLessonType();
        if (type == LessonType.FLASHCARD || type == LessonType.QCM) {
            return itemCount >= 5 && itemCount <= 20;
        }
        
        if (type == LessonType.MATCHING_PAIR || type == LessonType.SORTING_EXERCISE) {
            return itemCount >= 3 && itemCount <= 10;
        }
        
        return false;
    }
}
