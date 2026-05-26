package com.glotrush.constants;

public class CronConstants {
    
    private CronConstants() {}

    public static final String LESSON_REMINDER_CRON = "0 0 12 * * ?";
    public static final String STREAK_REMINDER_CRON = "0 0 21 * * ?";
    public static final String INACTIVITY_REMINDER_CRON = "0 0 9 * * ?";
    public static final String REVIEW_REMINDER_CRON = "0 0 18 * * ?";
    public static final String WEEKLY_GOAL_CRON = "0 0 8 ? * MON";
}
