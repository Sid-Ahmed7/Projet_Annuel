package com.glotrush.dto.request.exercice;

import com.glotrush.dto.request.LessonRequest;
import com.glotrush.entities.lesson.QcmLesson;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class QcmQuestionRequest extends LessonRequest {
    @NotNull
    @NotEmpty
    private String question;
    @NotNull
    @NotEmpty
    private List<String> options; // Réponses possibles
    @NotNull
    private Integer correctOptionIndex; // Index de la bonne réponse
    @NotNull
    @NotEmpty
    private String explanation; // Explication de la réponse
}