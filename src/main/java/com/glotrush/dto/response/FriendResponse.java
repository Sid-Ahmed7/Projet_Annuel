package com.glotrush.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponse {

    private UUID id;
    private UUID accountId;
    private String username;
    private String photoUrl;
    private Integer currentStreak;
    private LocalDateTime friendSince;

}
