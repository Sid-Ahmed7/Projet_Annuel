package com.glotrush.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakResponse {

    private int currentStreak;
    private int longestStreak;
    private LocalDate lastActivityDate;
    
}
