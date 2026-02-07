package com.glotrush.dto.request.exercice;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.entities.lesson.FlashcardLesson;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class FlashcardRequest extends LessonRequest {
    @NotNull
    @NotEmpty
    private String front;
    @NotNull
    @NotEmpty
    private String back;
    @NotNull
    @NotEmpty
    private String frontLanguage;
    @NotNull
    @NotEmpty
    private String backLanguage;
}