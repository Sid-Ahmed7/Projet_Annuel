package com.glotrush.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.glotrush.builder.FriendsBuilder;
import com.glotrush.config.TestMessageSourceConfig;
import com.glotrush.dispatcher.notifications.NotificationDispatcher;
import com.glotrush.dto.response.AccountSearchResponse;
import com.glotrush.dto.response.FriendRequestResponse;
import com.glotrush.dto.response.FriendResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Friends;
import com.glotrush.enumerations.FriendRequestStatus;
import com.glotrush.exceptions.CannotAddYourselfException;
import com.glotrush.exceptions.UserNotFoundException;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.FriendsRepository;
import com.glotrush.repositories.UserProfileRepository;
import com.glotrush.services.friends.FriendsService;
import com.glotrush.services.friends.IFriendsService;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(classes = TestMessageSourceConfig.class)
@DisplayName("FriendsService Unit Tests")
public class FriendsServiceTest {

    @Mock
    private FriendsRepository friendsRepository;
    @Mock
    private AccountsRepository accountsRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private FriendsBuilder friendsBuilder;
    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private MessageSource messageSource;

    private IFriendsService friendsService;

    private UUID senderId;
    private UUID receiverId;
    private UUID friendId;

    private Accounts sender;
    private Accounts receiver;
    private Friends request;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        friendsService = new FriendsService(friendsRepository, accountsRepository, userProfileRepository, messageSource, friendsBuilder, notificationDispatcher);
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        friendId = UUID.randomUUID();

        sender = Accounts.builder().id(senderId).username("Gojo").email("gojo@gmail.com").build();
        receiver = Accounts.builder().id(receiverId).username("Geto").email("geto@gmail.com").build();

        request = Friends.builder()
            .id(friendId)
            .sender(sender)
            .receiver(receiver)
            .status(FriendRequestStatus.PENDING)
            .build();
    }

    @Test
    @DisplayName("Should send friend request successfully")
    void sendFriendRequest_Success() {
        FriendRequestResponse friendResponse = new FriendRequestResponse();
        when(accountsRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(friendsRepository.findBetweenTwoUsers(senderId, receiverId)).thenReturn(Optional.empty());
        when(accountsRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(friendsRepository.save(any(Friends.class))).thenReturn(request);
        when(friendsBuilder.toFriendRequestResponse(any(), any())).thenReturn(friendResponse);
        FriendRequestResponse result = friendsService.sendFriendRequest(senderId, receiverId);
        assertThat(result).isNotNull();
        verify(friendsRepository).save(any(Friends.class));
        verify(notificationDispatcher).sendFriendRequestReceived(sender, receiver);
    }

    @Test
    @DisplayName("Should not send friend request when sender and receiver are the same and throw exception")
    void sendFriendRequest_SameUser() {
        assertThatThrownBy(() -> friendsService.sendFriendRequest(senderId, senderId)).isInstanceOf(CannotAddYourselfException.class);
        verify(friendsRepository, never()).save(any(Friends.class));
    }   

    @Test
    @DisplayName("Should throw UserNotFoundException when receiver not found")
    void sendFriendRequest_receiverNotFound() {
        when(accountsRepository.findById(receiverId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> friendsService.sendFriendRequest(senderId, receiverId)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Should accept friend request successfully")
    void acceptFriendRequest_Success() {
        FriendResponse expected = new FriendResponse();
        when(friendsRepository.findById(friendId)).thenReturn(Optional.of(request));
        when(friendsRepository.save(any(Friends.class))).thenReturn(request);
        when(accountsRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(accountsRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(friendsBuilder.toFriendResponse(any(), any())).thenReturn(expected);
        FriendResponse result = friendsService.acceptRequest(receiverId, friendId);
        assertThat(result).isNotNull();
        verify(notificationDispatcher).sendFriendRequestAccepted(receiver, sender);
    }

    @Test
    @DisplayName("Should decline friend request successfully")
    void declineFriendRequest_Success() {
        when(friendsRepository.findById(friendId)).thenReturn(Optional.of(request));
        friendsService.declineRequest(receiverId, friendId);
        verify(friendsRepository).delete(request);
    }

    @Test
    @DisplayName("Should cancel friend request successfully")
    void cancelFriendRequest_Success() {
        when(friendsRepository.findById(friendId)).thenReturn(Optional.of(request));
        friendsService.cancelRequest(senderId, friendId);
        verify(friendsRepository).delete(request);
    }

    @Test
    @DisplayName("Should remove friend successfully")
    void removeFriend_Success() {
        when(friendsRepository.findById(friendId)).thenReturn(Optional.of(request));
        friendsService.removeFriend(senderId, friendId);
        verify(friendsRepository).delete(request);
    }

    @Test
    @DisplayName("Should return empty friends list when any query parameter is empty")
    void search_EmptyList() {
        List<AccountSearchResponse> result = friendsService.searchAccounts(senderId, "");
        assertThat(result).isEmpty();
        verify(accountsRepository, never()).searchUsers(any(), any());
    }

    @Test
    @DisplayName("Should return empty friends list when any query parameter is null")
    void search_EmptyList_NullQuery() {
        List<AccountSearchResponse> result = friendsService.searchAccounts(senderId, null);
        assertThat(result).isEmpty();
        verify(accountsRepository, never()).searchUsers(any(), any());
    }

    @Test
    @DisplayName("Should return search results successfully")
    void search_Success() {
        when(accountsRepository.searchUsers(eq("geto"), any(Pageable.class))).thenReturn(List.of(receiver));
        when(userProfileRepository.findByAccount_Id(receiverId)).thenReturn(Optional.empty());
        when(friendsRepository.findBetweenTwoUsers(senderId, receiverId)).thenReturn(Optional.empty());
        List<AccountSearchResponse> result = friendsService.searchAccounts(senderId, "geto");
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getAccountId()).isEqualTo(receiverId);
    }

}
