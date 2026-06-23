package com.glotrush.dto.response;

import java.util.UUID;

import com.glotrush.enumerations.FriendsViewStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSearchResponse {
    private UUID accountId;
    private String username;
    private String photoUrl;
    private String bio;
    private FriendsViewStatus friendsViewStatus;
    private UUID friendRequestId;
}
