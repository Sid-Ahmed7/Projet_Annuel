package com.glotrush.builder;

import org.springframework.stereotype.Component;

import com.glotrush.dto.response.FriendRequestResponse;
import com.glotrush.dto.response.FriendResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Friends;
import com.glotrush.repositories.UserProfileRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FriendsBuilder {

    private final UserProfileRepository userProfileRepository;

    public FriendResponse toFriendResponse(Friends friend, Accounts account) {
        return FriendResponse.builder()
            .id(friend.getId())
            .accountId(account.getId())
            .username(account.getUsername())
            .photoUrl(userProfileRepository.findByAccount_Id(account.getId()).map(profile -> profile.getPhotoUrl()).orElse(null))
            .currentStreak(account.getCurrentStreak())
            .friendSince(friend.getUpdatedAt())
            .build();
    }

    public FriendRequestResponse toFriendRequestResponse(Friends friend, Accounts account) {
        return FriendRequestResponse.builder()
            .id(friend.getId())
            .accountId(account.getId())
            .username(account.getUsername())
            .photoUrl(userProfileRepository.findByAccount_Id(account.getId()).map(profile -> profile.getPhotoUrl()).orElse(null))
            .createdAt(friend.getCreatedAt())
            .build();
    }
}
