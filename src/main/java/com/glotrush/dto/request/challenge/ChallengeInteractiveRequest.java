package com.glotrush.dto.request.challenge;

import java.util.List;

import com.glotrush.enumerations.InteractiveSystemType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChallengeInteractiveRequest {

    private String questionText;

    private List<String> imagePaths;

    private List<String> audioPaths;

    @NotNull
    private InteractiveSystemType systemType;

    private List<String> options;

    private Integer correctOptionIndex;

    private String correctWord;
}
