package com.glotrush.dto.response.stats;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEngagementResponse {
    private Long totalUsers;
    private Long newUsersLastSevenDays;
    private Long activeUsersLastSevenDays;
    private Long usersWithActiveStreak;
    private List<UserGrowthByMonth> newUsersByMonth;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserGrowthByMonth {
        private String month;
        private Long count;
    }
}