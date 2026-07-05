package com.glotrush.dto.response.export;

import java.time.LocalDateTime;

import com.glotrush.enumerations.ChallengeStatus;
import com.glotrush.enumerations.ChallengeType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeExportResponse {
    private String title;
    private ChallengeType type;      
    private ChallengeStatus status; 
    private LocalDateTime createdAt;
}
