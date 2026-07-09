package com.glotrush.constants;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final int PASSWORD_EXPIRY_DAYS = 60;
    public  static final String REGEX_UPPER = ".*[A-Z].*";
    public  static final String REGEX_LOWER = ".*[a-z].*";
    public  static final String REGEX_DIGIT = ".*\\d.*";
    public  static final String REGEX_SPECIAL = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*";
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int ACCOUNT_LOCK_DURATION_MINUTES = 15;
    public static final String RANDOM_NUMBERS_SIX_CODE= "%06d";
    public static final String NOTIFICATION_KEY_TYPE = "sse.notifications";
    public static final String X_QUEUE_KEY_TYPE = "x-queue-master-locator";
}
