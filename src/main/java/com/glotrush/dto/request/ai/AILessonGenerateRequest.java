package com.glotrush.dto.request.ai;

import com.glotrush.enumerations.LessonType;
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
}
