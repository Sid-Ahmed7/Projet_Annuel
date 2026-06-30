package com.glotrush.dto.request.exercice;

import com.glotrush.enumerations.InteractiveSystemType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class InteractiveQuestionRequest {

    private UUID id;

    private String questionText;

    private List<String> imagePaths;

    private List<String> audioPaths;

    @NotNull
    private InteractiveSystemType systemType;

    private List<String> options;

    private Integer correctOptionIndex;

    private String correctWord;
}
