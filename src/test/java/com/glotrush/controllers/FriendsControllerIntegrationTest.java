package com.glotrush.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glotrush.dto.request.LoginRequest;
import com.glotrush.dto.response.FriendRequestResponse;
import com.glotrush.dto.response.FriendResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.enumerations.AccountStatus;
import com.glotrush.enumerations.UserRole;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.services.friends.IFriendsService;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("FriendsController Integration Tests")
public class FriendsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private IFriendsService friendsService;
    
    private static final String TEST_EMAIL = "test@gmail.com";
    private static final String TEST_PASSWORD = "SecurePass123!@#";
    private static final String TEST_USERNAME = "GojoSatoru";

    private static final String TEST_EMAIL_SECOND = "second@gmail.com";
    private static final String TEST_PASSWORD_SECOND = "SecondPass123!@#";
    private static final String TEST_USERNAME_SECOND = "MegumiFushiguro";

    private UUID accountId;
    private UUID secondAccountId;
    private Accounts account;
    private Accounts secondAccount;

    @BeforeEach
    void setup() {

        accountsRepository.deleteAll();

        account = Accounts.builder()
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .username(TEST_USERNAME)
                .firstName("Satoru")
                .lastName("Gojo")
                .role(UserRole.USER)
                .status(AccountStatus.ACTIVE)
                .failedLoginAttempts(0)
                .lastPasswordChange(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        secondAccount = Accounts.builder()
                .email(TEST_EMAIL_SECOND)
                .password(passwordEncoder.encode(TEST_PASSWORD_SECOND))
                .username(TEST_USERNAME_SECOND)
                .firstName("Megumi")
                .lastName("Fushiguro")
                .role(UserRole.USER)
                .status(AccountStatus.ACTIVE)
                .failedLoginAttempts(0)
                .lastPasswordChange(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        accountsRepository.save(account);
        accountId = account.getId();

        accountsRepository.save(secondAccount);
        secondAccountId = secondAccount.getId();
    }


    private Cookie logAndGetCookie() throws Exception {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

                return result.getResponse().getCookie("access_token");
    }


    @Test
    @DisplayName("Should return list of friends")
    void shouldReturnListOfFriends() throws Exception {
        Cookie cookie = logAndGetCookie();
        when(friendsService.getFriendsList(any(UUID.class))).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/friends/list")
                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should return list of friends when not authenticated")
    void shouldReturnListOfFriendsUnauthorized() throws Exception {
        when(friendsService.getFriendsList(any(UUID.class))).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/friends/list")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should get pending friend requests")
    void getPendingRequests_ok() throws Exception {
        Cookie cookie = logAndGetCookie();
        when(friendsService.getPendingRequests(any(UUID.class))).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/friends/list/pending-requests")
                .cookie(cookie))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return when not authenticated")
    void getPendingRequests_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/friends/list/pending-requests")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should get sent friend requests")
    void getSentRequests_ok() throws Exception {
        Cookie cookie = logAndGetCookie();
        when(friendsService.getSentRequests(any(UUID.class))).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/friends/list/sent-requests")
                .cookie(cookie))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return when not authenticated")
    void getSentRequests_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/friends/list/sent-requests")).andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("Should search accounts ")
    void searchAccounts_ok() throws Exception {
        Cookie authCookie = logAndGetCookie();
        when(friendsService.searchAccounts(any(UUID.class), any(String.class))).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/friends/search")
                .cookie(authCookie)
                .param("query", TEST_USERNAME_SECOND))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return unauthorized when searching accounts without authentication")
    void searchAccounts_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/friends/search")
                .param("query", TEST_USERNAME_SECOND))
                .andExpect(status().isUnauthorized());
    }

    


    @Test
    @DisplayName("Should send friend request")
    void sendFriendRequest_ok() throws Exception {
        Cookie cookie = logAndGetCookie();
        when(friendsService.sendFriendRequest(any(UUID.class), any(UUID.class)))
                .thenReturn(new FriendRequestResponse());

        mockMvc.perform(post("/api/v1/friends/send-request")
                .cookie(cookie)
                .param("receiverId", secondAccountId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return unauthorized when sending friend request without authentication")
    void sendFriendRequest_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/friends/send-request")
                .param("receiverId", secondAccountId.toString()))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("Should accept friend request")
    void acceptFriendRequest_ok() throws Exception {
        Cookie cookie = logAndGetCookie();
        when(friendsService.acceptRequest(any(UUID.class), any(UUID.class))).thenReturn(new FriendResponse());

        mockMvc.perform(put("/api/v1/friends/accept-request")
                .cookie(cookie)
                .param("requestId", secondAccountId.toString()))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("Should decline friend request")
    void declineFriendRequest_noContent() throws Exception {
        Cookie cookie = logAndGetCookie();
        doNothing().when(friendsService).declineRequest(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/api/v1/friends/decline-request")
                .cookie(cookie)
                .param("requestId", secondAccountId.toString()))
                .andExpect(status().isNoContent());
    }


    @Test
    @DisplayName("Should cancel friend request")
    void cancelFriendRequest_noContent() throws Exception {
        Cookie cookie = logAndGetCookie();
        doNothing().when(friendsService).cancelRequest(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/api/v1/friends/cancel-request")
                .cookie(cookie)
                .param("requestId", secondAccountId.toString()))
                .andExpect(status().isNoContent());
    }


    @Test
    @DisplayName("Should remove friend")
    void removeFriend_noContent() throws Exception {
        Cookie cookie = logAndGetCookie();
        doNothing().when(friendsService).removeFriend(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/api/v1/friends/remove")
                .cookie(cookie)
                .param("friendId", secondAccountId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return unauthorized when removing friend without authentication")
    void removeFriend_unauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/friends/remove")
                .param("friendId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }







}
