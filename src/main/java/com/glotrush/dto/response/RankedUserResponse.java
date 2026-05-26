package com.glotrush.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankedUserResponse {
    
    private Integer rank;
    private UUID accountId;
    private String username;
    private String photoUrl;
    private Long totalXP;
    private Integer level;
    private Double levelProgressPercentage;
    private Boolean isCurrentUser;
}
