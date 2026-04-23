package com.glotrush.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesRequest {
    
    private boolean lessonRemindersEnabled;
    private boolean streakUrgencyEnabled;
    private boolean inactivityEnabled;
    private boolean reviewRemindersEnabled;
    private boolean weeklyGoalRemindersEnabled;
}
