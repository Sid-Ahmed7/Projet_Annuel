package com.glotrush.utils;

public class LevelUtils {
    private LevelUtils() {}

    private static final int BASE_XP_PER_LEVEL = 100;

    private static long calculateXPForLevel(int level) {
        if(level <= 1){
            return 0;
        };
        long totalXP = 0;
        for (int i = 1; i < level; i++) {
            totalXP += BASE_XP_PER_LEVEL * (long) i;
        }
        return totalXP;

    }
    public static Integer calculateLevel(Long totalXP) {
        if (totalXP == null || totalXP < 0) return 1;
        int level = 1;
        while (totalXP >= calculateXPForLevel(level + 1)) {
            level++;
        }
        return level;
    }

    public static Long calculateCurrentLevelXP(Long totalXP) {
        if (totalXP == null || totalXP < 0) return 0L;
        int level = calculateLevel(totalXP);
        return totalXP - calculateXPForLevel(level);
    }

    public static Long calculateNextLevelXP(Long totalXP) {
        int level = calculateLevel(totalXP);
        return calculateXPForLevel(level + 1) - totalXP;
    }

    public static Double calculateLevelProgressPercentage(Long totalXP) {
        if (totalXP == null || totalXP < 0) return 0.0;
        Long currentLevelXP = calculateCurrentLevelXP(totalXP);
        int level = calculateLevel(totalXP);
        long xpForNextLevel = calculateXPForLevel(level + 1) - calculateXPForLevel(level);
        return (currentLevelXP.doubleValue() / xpForNextLevel) * 100.0;
    }
}
