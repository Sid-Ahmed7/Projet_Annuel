package com.glotrush.services.admin;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.response.UserStatisticsResponse;

public interface IAdminStatisticsService {

    List<UserStatisticsResponse> getAllUsersStatistics();

    UserStatisticsResponse getUserStatisticsById(UUID userId);
}
