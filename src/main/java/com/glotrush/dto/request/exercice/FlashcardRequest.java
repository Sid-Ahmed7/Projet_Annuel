package com.glotrush.dto.request.exercice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class FlashcardRequest {
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