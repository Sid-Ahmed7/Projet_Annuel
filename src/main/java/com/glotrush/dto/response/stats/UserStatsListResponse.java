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
public class UserStatsListResponse {
    private List<UserStatsResponse> users;
    private Long totalUsers;
    private Integer currentPage;
    private Integer pageSize;
}
