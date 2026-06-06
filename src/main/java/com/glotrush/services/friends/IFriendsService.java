package com.glotrush.services.friends;

import java.util.List;
import java.util.UUID;

import com.glotrush.dto.response.AccountSearchResponse;
import com.glotrush.dto.response.FriendRequestResponse;
import com.glotrush.dto.response.FriendResponse;

public interface IFriendsService {

    FriendRequestResponse sendFriendRequest(UUID senderId, UUID receiverId);
    FriendResponse acceptRequest(UUID accountId, UUID friendId);
    void declineRequest(UUID accountId, UUID friendId);
    void cancelRequest(UUID senderId, UUID friendId);
    void removeFriend(UUID accountId, UUID friendId);
    List<FriendResponse> getFriendsList(UUID accountId);
    List<FriendRequestResponse> getPendingRequests(UUID accountId);
    List<FriendRequestResponse> getSentRequests(UUID accountId);
    List<AccountSearchResponse> searchAccounts(UUID accountId, String query);
}
