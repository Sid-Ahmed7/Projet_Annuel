package com.glotrush.dto.response.exercice;

import com.glotrush.dto.request.LessonRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FlashcardResponse extends LessonRequest {
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