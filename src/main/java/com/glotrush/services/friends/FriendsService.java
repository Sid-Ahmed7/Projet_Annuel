package com.glotrush.services.friends;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.glotrush.builder.FriendsBuilder;
import com.glotrush.dto.response.AccountSearchResponse;
import com.glotrush.dto.response.FriendRequestResponse;
import com.glotrush.dto.response.FriendResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Friends;
import com.glotrush.entities.UserProfile;
import com.glotrush.enumerations.FriendRequestStatus;
import com.glotrush.enumerations.FriendsViewStatus;
import com.glotrush.exceptions.CannotAddYourselfException;
import com.glotrush.exceptions.FriendsAlreadyExistsException;
import com.glotrush.exceptions.FriendsNotFoundException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.FriendsRepository;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.dispatcher.notifications.NotificationDispatcher;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendsService implements IFriendsService {

    private final FriendsRepository friendsRepository;
    private final AccountsRepository accountsRepository;
    private final UserProfileRepository userProfileRepository;
    private final MessageSource messageSource;
    private final FriendsBuilder friendsResponseBuilder;
    private final NotificationDispatcher notificationDispatcher;

    @Override
    @Transactional
    public FriendRequestResponse sendFriendRequest(UUID senderId, UUID receiverId) {
 
        if(senderId.equals(receiverId)) {
            throw new CannotAddYourselfException(messageSource.getMessage("error.cannot_add_yourself", null, LocaleUtils.getCurrentLocale()));
        }

        Accounts receiver = accountsRepository.findById(receiverId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.account_not_found", null, LocaleUtils.getCurrentLocale())));
        friendsRepository.findBetweenTwoUsers(senderId, receiverId).ifPresent(friend -> {
            throw new FriendsAlreadyExistsException(messageSource.getMessage("error.friend_request_exists", null, LocaleUtils.getCurrentLocale()));
        });

        Accounts sender = accountsRepository.findById(senderId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.account_not_found", null, LocaleUtils.getCurrentLocale())));

        Friends friends = Friends.builder()
            .sender(sender)
            .receiver(receiver)
            .status(FriendRequestStatus.PENDING)
            .build();

        friends = friendsRepository.save(friends);
        notificationDispatcher.sendFriendRequestReceived(sender, receiver);

        return friendsResponseBuilder.toFriendRequestResponse(friends, receiver);
    }

    @Override
    @Transactional
    public FriendResponse acceptRequest(UUID accountId, UUID friendId) {
        Friends friends = friendsRepository.findById(friendId).orElseThrow(() -> new FriendsNotFoundException(messageSource.getMessage("error.friend_request_not_found", null, LocaleUtils.getCurrentLocale())));

        if (!friends.getReceiver().getId().equals(accountId)) {
            throw new FriendsNotFoundException(messageSource.getMessage("error.friend_request_not_found", null, LocaleUtils.getCurrentLocale()));
        }

        if(friends.getStatus() != FriendRequestStatus.PENDING) {
            throw new FriendsNotFoundException(messageSource.getMessage("error.friend_request_not_found", null, LocaleUtils.getCurrentLocale()));
        }

        friends.setStatus(FriendRequestStatus.ACCEPTED);
        friends = friendsRepository.save(friends);

        Accounts receiver = accountsRepository.findById(friends.getReceiver().getId()).orElseThrow();
        Accounts sender = accountsRepository.findById(friends.getSender().getId()).orElseThrow();
        notificationDispatcher.sendFriendRequestAccepted(receiver, sender);

        return friendsResponseBuilder.toFriendResponse(friends, sender);
    }

    @Override
    @Transactional
    public void declineRequest(UUID accountId, UUID friendId) {
        Friends friends = friendsRepository.findById(friendId).orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("error.friend_request_not_found", null, LocaleUtils.getCurrentLocale())));
    
        if (!friends.getReceiver().getId().equals(accountId)) {
            throw new FriendsNotFoundException(messageSource.getMessage("error.friend_request_not_found", null, LocaleUtils.getCurrentLocale()));
        }
        friendsRepository.delete(friends);
    }

    @Override
    @Transactional
    public void cancelRequest(UUID senderId, UUID friendId) {
        Friends friends = friendsRepository.findById(friendId).orElseThrow(() -> new FriendsNotFoundException(messageSource.getMessage("error.friend_request_not_found", null, LocaleUtils.getCurrentLocale())));

        if (!friends.getSender().getId().equals(senderId)) {
            throw new FriendsNotFoundException(messageSource.getMessage("error.friend_request_not_found", null, LocaleUtils.getCurrentLocale()));
        }

        if (friends.getStatus() != FriendRequestStatus.PENDING) {
            throw new FriendsNotFoundException(messageSource.getMessage("error.friend_request_not_found", null, LocaleUtils.getCurrentLocale()));
        }

        friendsRepository.delete(friends);
    }

    @Override
    @Transactional
    public void removeFriend(UUID accountId, UUID friendId) {
        Friends friends = friendsRepository.findById(friendId).orElseThrow(() -> new FriendsNotFoundException(messageSource.getMessage("error.friend_request_not_found", null, LocaleUtils.getCurrentLocale())));

        boolean isSender = friends.getSender().getId().equals(accountId);
        boolean isReceiver = friends.getReceiver().getId().equals(accountId);

        if (!isSender && !isReceiver) {
            throw new FriendsNotFoundException(messageSource.getMessage("error.friend_request_not_found", null, LocaleUtils.getCurrentLocale()));
        }
        friendsRepository.delete(friends);
    }

    @Override
    public List<FriendResponse> getFriendsList(UUID accountId) {
        return friendsRepository.findAcceptedRequests(accountId).stream()
            .map(friends -> {
                Accounts friendAccount = friends.getSender().getId().equals(accountId) ? friends.getReceiver() : friends.getSender();
                return friendsResponseBuilder.toFriendResponse(friends, friendAccount);
            }).toList();
    }

    @Override
    public List<FriendRequestResponse> getPendingRequests(UUID accountId) {
        return friendsRepository.findByReceiver_IdAndStatus(accountId, FriendRequestStatus.PENDING).stream()
            .map(friends -> friendsResponseBuilder.toFriendRequestResponse(friends, friends.getSender()))
            .toList();
    }

    @Override
    public List<FriendRequestResponse> getSentRequests(UUID accountId) {
        return friendsRepository.findBySender_IdAndStatus(accountId, FriendRequestStatus.PENDING).stream()
            .map(friends -> friendsResponseBuilder.toFriendRequestResponse(friends, friends.getReceiver()))
            .toList();
    }

    @Override
    public List<AccountSearchResponse> searchAccounts(UUID accountId, String query) {

        if(query == null || query.isBlank()) {
            return List.of();
        }
        return accountsRepository.searchUsers(query, PageRequest.of(0, 10)).stream()
            .filter(account -> !account.getId().equals(accountId))
            .map(account -> {
                Optional<UserProfile> userProfile = userProfileRepository.findByAccount_Id(account.getId());
                String photoUrl = userProfile.map(profile -> profile.getPhotoUrl()).orElse(null);
                String bio = userProfile.map(profile -> profile.getBio()).orElse(null);

                UUID friendRequestId = null;
                FriendsViewStatus friendsStatus = FriendsViewStatus.NONE;

                Optional<Friends> existing = friendsRepository.findBetweenTwoUsers(accountId, account.getId());
                if (existing.isPresent()) {
                    Friends friends = existing.get();
                    friendRequestId = friends.getId();
                    if (friends.getStatus() == FriendRequestStatus.ACCEPTED) {
                        friendsStatus = FriendsViewStatus.ACCEPTED;
                    } else if (friends.getSender().getId().equals(accountId)) {
                        friendsStatus = FriendsViewStatus.PENDING_SENT;
                    } else {
                        friendsStatus = FriendsViewStatus.PENDING_RECEIVED;
                    }
                }

                return AccountSearchResponse.builder()
                    .accountId(account.getId())
                    .username(account.getUsername())
                    .photoUrl(photoUrl)
                    .bio(bio)
                    .friendsViewStatus(friendsStatus)
                    .friendRequestId(friendRequestId)
                    .build();
            })
            .toList();
    }
    
}
