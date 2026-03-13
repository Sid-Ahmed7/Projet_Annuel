package com.glotrush.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionStatsResponse {

    private Long activeSubscribers;
    private Long premiumSubscribers;
    private Long freeSubscribers;
    private BigDecimal totalRevenue;
    private List<SubscriptionByMonth> subscriptionsByMonth;
    private List<SubscriptionByYear> subscriptionsByYear;   
    private SubscriptionByPlan subscriptionsByPlan;
    
    @Data
    @Builder
    public static class SubscriptionByMonth {
        private String month;
        private Long subscriberCount;
    }

    @Data
    @Builder
    public static class SubscriptionByYear {
        private int year;
        private Long subscriberCount;
    }

    @Data
    @Builder
    public static class SubscriptionByPlan {
        private Long freeCount;
        private Long premiumCount;
        private Double freePercentage;
        private Double premiumPercentage;
    }

    
}
