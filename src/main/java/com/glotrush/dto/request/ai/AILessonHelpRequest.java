package com.glotrush.dto.request.ai;

import com.glotrush.enumerations.AILessonHelpType;
import com.glotrush.enumerations.LessonType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AILessonHelpRequest {

    @NotNull
    private UUID lessonId;

    @NotNull
    private UUID exerciseId;

    @NotNull
    private LessonType exerciseType;

    @NotNull
    private AILessonHelpType helpType;
}
