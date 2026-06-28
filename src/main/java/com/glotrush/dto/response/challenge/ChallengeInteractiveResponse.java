package com.glotrush.dto.response.challenge;

import java.util.List;
import java.util.UUID;

import com.glotrush.enumerations.InteractiveSystemType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeInteractiveResponse {
    private UUID id;
    private String questionText;
    private List<String> imagePaths;
    private List<String> audioPaths;
    private InteractiveSystemType systemType;
    private List<String> options;
    private Integer correctOptionIndex;
    private String correctWord;
}
