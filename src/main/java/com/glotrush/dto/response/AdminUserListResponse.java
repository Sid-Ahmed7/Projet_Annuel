package com.glotrush.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListResponse {
    private List<AdminUserDetailsResponse> users;
    private Long totalUsers;
    private Integer currentPage;
    private Integer pageSize;
}
