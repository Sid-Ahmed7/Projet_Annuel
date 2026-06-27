package com.glotrush.services.admin;

import java.util.UUID;

import com.glotrush.dto.response.stats.LearningStatsResponse;
import com.glotrush.dto.response.stats.UserDetailsResponse;
import com.glotrush.dto.response.stats.UserEngagementResponse;
import com.glotrush.dto.response.stats.UserStatsListResponse;

public interface IAdminStatsService {
    UserEngagementResponse getUserEngagement();
    LearningStatsResponse getLearningMetrics();
    UserStatsListResponse getUserStats(String search, Integer page, Integer size);
    UserDetailsResponse getUserDetail(UUID accountId);
}