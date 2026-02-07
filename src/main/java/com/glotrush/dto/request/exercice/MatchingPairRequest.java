package com.glotrush.dto.request.exercice;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.entities.lesson.MatchingPairLesson;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class MatchingPairRequest extends LessonRequest {
    @NotNull
    @NotEmpty
    private String item1;
    @NotNull
    @NotEmpty
    private String item2;
}
