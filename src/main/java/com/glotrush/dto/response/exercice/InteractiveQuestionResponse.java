package com.glotrush.dto.response.exercice;

import com.glotrush.enumerations.InteractiveSystemType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class InteractiveQuestionResponse {
    private UUID id;
    private String questionText;
    private List<String> imagePaths;
    private List<String> audioPaths;
    private InteractiveSystemType systemType;
    private List<String> options;
    private Integer correctOptionIndex;
    private String correctWord;
}
