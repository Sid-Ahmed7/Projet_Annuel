package com.glotrush.dto.request.ai;

import com.glotrush.dto.request.LessonRequest;
import jakarta.validation.Valid;
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
}
