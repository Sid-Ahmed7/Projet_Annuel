package com.glotrush.mapping;

import com.glotrush.dto.response.export.*;
import com.glotrush.entities.*;
import com.glotrush.entities.challenge.Challenge;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ExportDataMapper {

    AccountExportResponse mapAccount(Accounts account);

    ProfileExportResponse mapProfile(UserProfile userProfile);

    @Mapping(source = "language.name", target = "language")
    @Mapping(source = "languageType", target = "type")
    @Mapping(source = "proficiencyLevel", target = "level")
    LanguageExportResponse mapLanguage(UserLanguage userLanguage);
    List<LanguageExportResponse> mapLanguages(List<UserLanguage> userLanguages);

    @Mapping(source = "topic.name", target = "topic")
    ProgressExportResponse mapProgress(UserProgress userProgress);
    List<ProgressExportResponse> mapProgressList(List<UserProgress> userProgressList);

    @Mapping(source = "lesson.title", target = "lesson")
    LessonProgressExportResponse mapLessonProgress(UserLessonProgress userLessonProgress);
    List<LessonProgressExportResponse> mapLessonProgressList(List<UserLessonProgress> userLessonProgressList);

    @Mapping(source = "lesson.title", target = "lesson")
    LessonSessionExportResponse mapLessonSession(LessonSession lessonSession);
    List<LessonSessionExportResponse> mapLessonSessions(List<LessonSession> lessonSessionList);

    UserMistakeExportResponse mapUserMistake(UserMistake userMistake);
    List<UserMistakeExportResponse> mapUserMistakes(List<UserMistake> userMistakeList);

    default FriendsExportResponse mapFriend(Friends friend, UUID accountId) {
        UUID friendId = friend.getSender().getId().equals(accountId) ? friend.getReceiver().getId() : friend.getSender().getId();
        return FriendsExportResponse.builder()
                .friendId(friendId)
                .since(friend.getCreatedAt())
                .build();
    }

    default List<FriendsExportResponse> mapFriends(List<Friends> friendsList, UUID accountId) {
        return friendsList.stream().map(friend -> mapFriend(friend, accountId)).toList();
    }

    @Mapping(source = "challengeType", target = "type")
    @Mapping(source = "challengeStatus", target = "status")
    ChallengeExportResponse mapChallenge(Challenge challenge);
    List<ChallengeExportResponse> mapChallenges(List<Challenge> challengesList);

    @Mapping(source = "plan.name", target = "plan")
    SubscriptionExportResponse mapSubscription(Subscription subscription);

    @Mapping(source = "topic.name", target = "topic")
    TopicReviewExportResponse mapTopicReview(TopicReview topicReview);
    List<TopicReviewExportResponse> mapTopicReviews(List<TopicReview> topicReviewList);

    @Mapping(source = "paymentStatus", target = "status")
    PaymentHistoryExportResponse mapPaymentHistory(PaymentHistory paymentHistory);
    List<PaymentHistoryExportResponse> mapPaymentHistoryList(List<PaymentHistory> paymentHistoryList);
}