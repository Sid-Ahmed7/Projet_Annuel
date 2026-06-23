package com.glotrush.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.glotrush.dto.response.AccountSearchResponse;
import com.glotrush.dto.response.FriendRequestResponse;
import com.glotrush.dto.response.FriendResponse;
import com.glotrush.services.friends.IFriendsService;
import com.glotrush.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendsController {

    private final IFriendsService friendsService;

    @GetMapping("/list")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<FriendResponse>> getFriendsList(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);        
        return ResponseEntity.ok(friendsService.getFriendsList(accountId));
    }

    @GetMapping("/list/pending-requests")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<FriendRequestResponse>> getPendingRequests(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(friendsService.getPendingRequests(accountId));
    }

    @GetMapping("/list/sent-requests")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<FriendRequestResponse>> getSentRequests(Authentication authentication) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(friendsService.getSentRequests(accountId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AccountSearchResponse>> searchAccounts(Authentication authentication, @RequestParam String query) {
        UUID accountId = SecurityUtils.extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(friendsService.searchAccounts(accountId, query));
    }

    @PostMapping("/send-request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FriendRequestResponse> sendFriendRequest(Authentication authentication, @RequestParam UUID receiverId) {
        UUID senderId = SecurityUtils.extractUserIdFromAuth(authentication);
        FriendRequestResponse response = friendsService.sendFriendRequest(senderId, receiverId);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/accept-request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FriendResponse> acceptFriendRequest(Authentication authentication, @RequestParam UUID requestId) {
        UUID userId = SecurityUtils.extractUserIdFromAuth(authentication);
        FriendResponse response = friendsService.acceptRequest(userId, requestId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/decline-request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> declineFriendRequest(Authentication authentication, @RequestParam UUID requestId) {
        UUID userId = SecurityUtils.extractUserIdFromAuth(authentication);
        friendsService.declineRequest(userId, requestId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cancel-request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> cancelFriendRequest(Authentication authentication, @RequestParam UUID requestId) {
        UUID userId = SecurityUtils.extractUserIdFromAuth(authentication);
        friendsService.cancelRequest(userId, requestId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/remove")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeFriend(Authentication authentication, @RequestParam UUID friendId) {
        UUID userId = SecurityUtils.extractUserIdFromAuth(authentication);
        friendsService.removeFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }
}

