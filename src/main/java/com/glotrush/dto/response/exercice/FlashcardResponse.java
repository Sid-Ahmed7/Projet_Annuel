package com.glotrush.dto.response.exercice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FlashcardResponse {
    private UUID id;
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