package com.glotrush.mapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.glotrush.dto.response.export.AccountExportResponse;
import com.glotrush.dto.response.export.ChallengeExportResponse;
import com.glotrush.dto.response.export.FriendsExportResponse;
import com.glotrush.dto.response.export.LanguageExportResponse;
import com.glotrush.dto.response.export.SubscriptionExportResponse;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Friends;
import com.glotrush.entities.Language;
import com.glotrush.entities.Plan;
import com.glotrush.entities.Subscription;
import com.glotrush.entities.UserLanguage;
import com.glotrush.entities.challenge.Challenge;
import com.glotrush.enumerations.ChallengeStatus;
import com.glotrush.enumerations.ChallengeType;
import com.glotrush.enumerations.LanguageType;
import com.glotrush.enumerations.ProficiencyLevel;
import com.glotrush.enumerations.SubscriptionStatus;
import com.glotrush.enumerations.UserRole;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExportDataMapper Unit Tests")
class ExportDataMapperTest {

    private final ExportDataMapper mapper = Mappers.getMapper(ExportDataMapper.class);

    @Test
    @DisplayName("Map Accounts to AccountExportResponse")
    void shouldMapAccount() {
        Accounts account = Accounts.builder()
            .email("sukuna@gmail.com")
            .firstName("Ryomen")
            .lastName("Sukuna")
            .username("SukunaTheKing")
            .createdAt(LocalDateTime.now())
            .currentStreak(5)
            .longestStreak(10)
            .role(UserRole.USER)
            .build();

        AccountExportResponse result = mapper.mapAccount(account);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("sukuna@gmail.com");
        assertThat(result.getFirstName()).isEqualTo("Ryomen");
        assertThat(result.getLastName()).isEqualTo("Sukuna");
        assertThat(result.getUsername()).isEqualTo("SukunaTheKing");
        assertThat(result.getCurrentStreak()).isEqualTo(5);
        assertThat(result.getLongestStreak()).isEqualTo(10);
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("Should return null when account is null")
    void shouldReturnNullWhenAccountIsNull() {
        assertThat(mapper.mapAccount(null)).isNull();
    }

    @Test
    @DisplayName("Should map UserLanguage to LanguageExportResponse")
    void shouldMapLanguage() {
        Language language = Language.builder().name("French").build();
        UserLanguage userLanguage = UserLanguage.builder()
            .language(language)
            .languageType(LanguageType.LEARNING)
            .proficiencyLevel(ProficiencyLevel.B2)
            .startedAt(LocalDateTime.now())
            .build();

        LanguageExportResponse result = mapper.mapLanguage(userLanguage);

        assertThat(result).isNotNull();
        assertThat(result.getLanguage()).isEqualTo("French");
        assertThat(result.getType()).isEqualTo(LanguageType.LEARNING);
        assertThat(result.getLevel()).isEqualTo(ProficiencyLevel.B2);
    }

    @Test
    @DisplayName("Should map a list of UserLanguage to a list of LanguageExportResponse")
    void shouldMapLanguagesList() {
        Language language = Language.builder().name("English").build();
        UserLanguage userLanguage = UserLanguage.builder()
            .language(language)
            .languageType(LanguageType.NATIVE)
            .proficiencyLevel(ProficiencyLevel.C2)
            .build();

        List<LanguageExportResponse> result = mapper.mapLanguages(List.of(userLanguage));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLanguage()).isEqualTo("English");
    }

    @Test
    @DisplayName("Should return null when UserLanguage is null")
    void shouldReturnNullWhenLanguageIsNull() {
        assertThat(mapper.mapLanguage(null)).isNull();
    }



    @Test
    @DisplayName("Should return receiver ID when account is sender")
    void shouldReturnReceiverIdWhenAccountIsSender() {
        UUID accountId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        Accounts sender = Accounts.builder().id(accountId).build();
        Accounts receiver = Accounts.builder().id(friendId).build();
        Friends friend = Friends.builder()
            .sender(sender)
            .receiver(receiver)
            .createdAt(LocalDateTime.now())
            .build();

        FriendsExportResponse result = mapper.mapFriend(friend, accountId);

        assertThat(result.getFriendId()).isEqualTo(friendId);
    }

    @Test
    @DisplayName("Should return sender ID when account is receiver")
    void shouldReturnSenderIdWhenAccountIsReceiver() {
        UUID accountId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        Accounts sender = Accounts.builder().id(friendId).build();
        Accounts receiver = Accounts.builder().id(accountId).build();
        Friends friend = Friends.builder()
            .sender(sender)
            .receiver(receiver)
            .createdAt(LocalDateTime.now())
            .build();

        FriendsExportResponse result = mapper.mapFriend(friend, accountId);

        assertThat(result.getFriendId()).isEqualTo(friendId);
    }

    @Test
    @DisplayName("Should map a list of Friends")
    void shouldMapFriendsList() {
        UUID accountId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        Accounts sender = Accounts.builder().id(accountId).build();
        Accounts receiver = Accounts.builder().id(friendId).build();
        Friends friend = Friends.builder()
            .sender(sender)
            .receiver(receiver)
            .createdAt(LocalDateTime.now())
            .build();

        List<FriendsExportResponse> result = mapper.mapFriends(List.of(friend), accountId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFriendId()).isEqualTo(friendId);
    }

    @Test
    @DisplayName("Should map Subscription to SubscriptionExportResponse")
    void shouldMapSubscription() {
        Plan plan = Plan.builder().name("PREMIUM").build();
        Subscription subscription = Subscription.builder()
            .plan(plan)
            .status(SubscriptionStatus.ACTIVE)
            .startDate(LocalDateTime.now())
            .build();

        SubscriptionExportResponse result = mapper.mapSubscription(subscription);

        assertThat(result).isNotNull();
        assertThat(result.getPlan()).isEqualTo("PREMIUM");
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return null when subscription is null")
    void shouldReturnNullWhenSubscriptionIsNull() {
        assertThat(mapper.mapSubscription(null)).isNull();
    }


    @Test
    @DisplayName("Should map Challenge to ChallengeExportResponse")
    void shouldMapChallenge() {
        Challenge challenge = Challenge.builder()
            .challengeType(ChallengeType.DUEL)
            .challengeStatus(ChallengeStatus.ACTIVE)
            .build();

        ChallengeExportResponse result = mapper.mapChallenge(challenge);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(ChallengeType.DUEL);
        assertThat(result.getStatus()).isEqualTo(ChallengeStatus.ACTIVE);
    }
}