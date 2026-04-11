package com.glotrush.utils;

import java.util.UUID;
import org.springframework.security.core.Authentication;


public class SecurityUtils {
    private SecurityUtils() {}

     public static UUID extractUserIdFromAuth(Authentication authentication) {
        String userId =  authentication.getName();
        return UUID.fromString(userId);
    }
}
