package com.glotrush.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glotrush.dto.request.ChangeSubscriptionRequest;
import com.glotrush.dto.request.LoginRequest;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Subscription;
import com.glotrush.enumerations.SubscriptionType;
import com.glotrush.enumerations.AccountStatus;
import com.glotrush.enumerations.UserRole;
import com.glotrush.repositories.AccountsRepository;
import com.glotrush.repositories.SubscriptionRepository;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubscriptionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    private Accounts account;
    private Subscription subscription;

    private static final String TEST_EMAIL="aze62955@gmail.com";
    private static final String TEST_PASSWORD="Password123!@#";
    private static final String TEST_USERNAME="Muryōkūsho";


    @BeforeEach
    void setup() {

        subscriptionRepository.deleteAll();
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

    private void createSubscriptionForAccount(Accounts accounts, SubscriptionType type) {
        subscription = Subscription.builder()
                .account(accounts)
                .subscriptionType(type)
                .isActive(true)
                .startDate(LocalDateTime.now())
                .endDate(type == SubscriptionType.PREMIUM ? LocalDateTime.now().plusSeconds(60) : null)
                .updatedDate(LocalDateTime.now())
                .build();
        subscriptionRepository.save(subscription);
    }

    @Test
    @DisplayName("Should get my subscription successfully")
    void getMySubscription() throws Exception {
        Accounts savedAccount = accountsRepository.save(account);
        createSubscriptionForAccount(savedAccount, SubscriptionType.FREE);

        Cookie cookie = logAndGetCookie();

        mockMvc.perform(get("/api/v1/subscriptions/my-subscription")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionType").value("FREE"))
                .andExpect(jsonPath("$.isActive").value(true));
    }
    
    @Test
    @DisplayName("Should return 401 when accessing subscription without authentication")
    void getMySubscriptionWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/subscriptions/my-subscription")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return PREMIUM subscription details")
    void getPremiumSubscription () throws Exception {
        Accounts savedAccount = accountsRepository.save(account);
        createSubscriptionForAccount(savedAccount, SubscriptionType.PREMIUM);

        Cookie cookie = logAndGetCookie();

        mockMvc.perform(get("/api/v1/subscriptions/my-subscription")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionType").value("PREMIUM"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.endDate").exists());
    }

    @Test
    @DisplayName("Should change subscription from FREE to PREMIUM ")
    void changeSubscription() throws Exception {
        Accounts saveAccounts = accountsRepository.save(account);
        createSubscriptionForAccount(saveAccounts, SubscriptionType.FREE);

        Cookie cookie = logAndGetCookie();

        ChangeSubscriptionRequest request = ChangeSubscriptionRequest.builder()
                .subscriptionType(SubscriptionType.PREMIUM)
                .build();
        mockMvc.perform(put("/api/v1/subscriptions/change-subscription")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionType").value("PREMIUM"))
                .andExpect(jsonPath("$.endDate").exists());
    }

    @Test
    @DisplayName("Should change subscription from PREMIUM to FREE successfully")
    void changeSubscriptionToFree() throws Exception {
        Accounts saveAccounts = accountsRepository.save(account);
        createSubscriptionForAccount(saveAccounts, SubscriptionType.PREMIUM);

        Cookie cookie = logAndGetCookie();

        ChangeSubscriptionRequest request = ChangeSubscriptionRequest.builder()
                .subscriptionType(SubscriptionType.FREE)
                .build();
        mockMvc.perform(put("/api/v1/subscriptions/change-subscription")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionType").value("FREE"));    
    }

    @Test
    @DisplayName("Should return 401 when changing subscription without authentication")
    void changeSubscriptionWithoutAuth() throws Exception {
        ChangeSubscriptionRequest request = ChangeSubscriptionRequest.builder()
                .subscriptionType(SubscriptionType.FREE)
                .build();
        mockMvc.perform(put("/api/v1/subscriptions/change-subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    } 
    
    @Test
    @DisplayName("Should return 400 when changing subscription with null type")
    void changeSubscriptionWithNullType() throws Exception {
      Accounts saveAccounts = accountsRepository.save(account);
      createSubscriptionForAccount(saveAccounts, SubscriptionType.FREE);
      Cookie cookie = logAndGetCookie();
      ChangeSubscriptionRequest request = new ChangeSubscriptionRequest();

        mockMvc.perform(put("/api/v1/subscriptions/change-subscription")
                .cookie(cookie)        
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when user has no subscription")
    void testChangeSubscriptionWhenNoSubscriptionExists() throws Exception {
        accountsRepository.save(account);

        Cookie cookie = logAndGetCookie();

        ChangeSubscriptionRequest request = ChangeSubscriptionRequest.builder()
                .subscriptionType(SubscriptionType.PREMIUM)
                .build();

        mockMvc.perform(put("/api/v1/subscriptions/change-subscription")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
