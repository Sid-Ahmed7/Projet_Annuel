package com.glotrush.utils;

import java.security.SecureRandom;

import com.glotrush.constants.SecurityConstants;

public class GenerateRandomCode {

    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomCode() {
        return String.format(SecurityConstants.RANDOM_NUMBERS_SIX_CODE, random.nextInt(1_000_000));
    }
    
}
