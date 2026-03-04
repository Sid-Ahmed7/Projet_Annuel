package com.glotrush.utils;

public class LevelUtils {

    private static final int BASE_XP_PER_LEVEL = 1000;

    public static Integer calculateLevel(Long totalXP) {
        if (totalXP == null || totalXP < 0) return 1;
        return (int) (totalXP / BASE_XP_PER_LEVEL) + 1;
    }

    public static Long calculateCurrentLevelXP(Long totalXP) {
        if (totalXP == null || totalXP < 0) return 0L;
        return totalXP % BASE_XP_PER_LEVEL;
    }

    public static Long calculateNextLevelXP(Integer level) {
        return (long) BASE_XP_PER_LEVEL;
    }

    public static Double calculateLevelProgressPercentage(Long totalXP) {
        if (totalXP == null || totalXP < 0) return 0.0;
        Long currentLevelXP = calculateCurrentLevelXP(totalXP);
        return (currentLevelXP.doubleValue() / BASE_XP_PER_LEVEL) * 100.0;
    }
}
