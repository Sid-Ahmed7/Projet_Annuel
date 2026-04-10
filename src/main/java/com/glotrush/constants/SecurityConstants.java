package com.glotrush.constants;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final int PASSWORD_EXPIRY_DAYS = 60;
    public  static final String REGEX_UPPER = ".*[A-Z].*";
    public  static final String REGEX_LOWER = ".*[a-z].*";
    public  static final String REGEX_DIGIT = ".*\\d.*";
    public  static final String REGEX_SPECIAL = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*";
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int ACCOUNT_LOCK_DURATION_MINUTES = 1;

}
