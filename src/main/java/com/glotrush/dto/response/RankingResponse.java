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
public class RankingResponse {

    private List<RankedUserResponse> rankedUser;
    private RankedUserResponse currentUserRank;
    private Long totalParticipants;
    private Integer currentPage;
    private Integer pageSize;
    private Long xpToNextRank;
}
