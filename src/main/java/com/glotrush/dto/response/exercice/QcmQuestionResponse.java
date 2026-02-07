package com.glotrush.dto.response.exercice;

import com.glotrush.dto.request.LessonRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class QcmQuestionResponse extends LessonRequest {
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