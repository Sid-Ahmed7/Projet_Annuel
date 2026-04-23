package com.glotrush.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationPreferencesResponse {
    private boolean lessonRemindersEnabled;
    private boolean streakUrgencyEnabled;
    private boolean inactivityEnabled;
    private boolean reviewRemindersEnabled;
    private boolean weeklyGoalRemindersEnabled;
}
