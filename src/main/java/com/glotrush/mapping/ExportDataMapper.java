package com.glotrush.mapping;

import com.glotrush.entities.*;
import com.glotrush.entities.challenge.Challenge;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExportDataMapper {

    public Map<String, Object> mapAccount(Accounts account) {
        Map<String, Object> mapUserAccount = new LinkedHashMap<>();
        mapUserAccount.put("email", account.getEmail());
        mapUserAccount.put("firstName", account.getFirstName());
        mapUserAccount.put("lastName", account.getLastName());
        mapUserAccount.put("username", account.getUsername());
        mapUserAccount.put("createdAt", account.getCreatedAt());
        mapUserAccount.put("currentStreak", account.getCurrentStreak());
        mapUserAccount.put("longestStreak", account.getLongestStreak());
        mapUserAccount.put("role", account.getRole());
        return mapUserAccount;
    }

    public Map<String, Object> mapProfile(UserProfile userProfile) {
        Map<String, Object> mapUserProfile = new LinkedHashMap<>();
        mapUserProfile.put("bio", userProfile.getBio());
        mapUserProfile.put("countryCode", userProfile.getCountryCode());
        mapUserProfile.put("timezone", userProfile.getTimezone());
        mapUserProfile.put("isPublic", userProfile.getIsPublic());
        mapUserProfile.put("hasCompletedOnboarding", userProfile.getHasCompletedOnboarding());
        return mapUserProfile;
    }

    public List<Map<String, Object>> mapLanguages(List<UserLanguage> userLanguages) {
        return userLanguages.stream().map(userLanguage -> {
            Map<String, Object> mapUserLanguage = new LinkedHashMap<>();
            mapUserLanguage.put("language", userLanguage.getLanguage() != null ? userLanguage.getLanguage().getName() : null);
            mapUserLanguage.put("type", userLanguage.getLanguageType());
            mapUserLanguage.put("level", userLanguage.getProficiencyLevel());
            mapUserLanguage.put("startedAt", userLanguage.getStartedAt());
            return mapUserLanguage;
        }).toList();
    }

    public List<Map<String, Object>> mapProgress(List<UserProgress> userProgressList) {
        return userProgressList.stream().map(userProgress -> {
            Map<String, Object> mapUserProgress = new LinkedHashMap<>();
            mapUserProgress.put("topic", userProgress.getTopic() != null ? userProgress.getTopic().getName() : null);
            mapUserProgress.put("totalXP", userProgress.getTotalXP());
            mapUserProgress.put("completedLessons", userProgress.getCompletedLessons());
            mapUserProgress.put("accuracy", userProgress.getAccuracy());
            mapUserProgress.put("examPassed", userProgress.getExamPassed());
            return mapUserProgress;
        }).toList();
    }

    public List<Map<String, Object>> mapLessonProgress(List<UserLessonProgress> userLessonProgressList) {
        return userLessonProgressList.stream().map(userLessonProgress -> {
            Map<String, Object> mapUserLessonProgress = new LinkedHashMap<>();
            mapUserLessonProgress.put("lesson", userLessonProgress.getLesson() != null ? userLessonProgress.getLesson().getTitle() : null);
            mapUserLessonProgress.put("status", userLessonProgress.getStatus());
            mapUserLessonProgress.put("totalAttempts", userLessonProgress.getTotalAttempts());
            mapUserLessonProgress.put("timeSpentSeconds", userLessonProgress.getTimeSpentSeconds());
            mapUserLessonProgress.put("completedAt", userLessonProgress.getCompletedAt());
            return mapUserLessonProgress;
        }).toList();
    }

    public List<Map<String, Object>> mapSessions(List<LessonSession> lessonSessionList) {
        return lessonSessionList.stream().map(lessonSession -> {
            Map<String, Object> mapLessonSession = new LinkedHashMap<>();
            mapLessonSession.put("lesson", lessonSession.getLesson() != null ? lessonSession.getLesson().getTitle() : null);
            mapLessonSession.put("correctAnswers", lessonSession.getCorrectAnswers());
            mapLessonSession.put("wrongAnswers", lessonSession.getWrongAnswers());
            mapLessonSession.put("totalTime", lessonSession.getTotalTime());
            mapLessonSession.put("completedAt", lessonSession.getCompletedAt());
            return mapLessonSession;
        }).toList();
    }

    public List<Map<String, Object>> mapMistakes(List<UserMistake> userMistakeList) {
        return userMistakeList.stream().map(userMistake -> {
            Map<String, Object> mapUserMistake = new LinkedHashMap<>();
            mapUserMistake.put("questionId", userMistake.getQuestionId());
            mapUserMistake.put("lessonType", userMistake.getLessonType());
            mapUserMistake.put("nextReviewAt", userMistake.getNextReviewAt());
            mapUserMistake.put("userAnswer", userMistake.getUserAnswer());
            mapUserMistake.put("isResolved", userMistake.getIsResolved());
            return mapUserMistake;
        }).toList();
    }

    public List<Map<String, Object>> mapFriends(List<Friends> friendsList, UUID accountId) {
        return friendsList.stream().map(friend -> {
            Map<String, Object> mapFriend = new LinkedHashMap<>();
            mapFriend.put("friendId", friend.getSender().getId().equals(accountId) ? friend.getReceiver().getId() : friend.getSender().getId());
            mapFriend.put("since", friend.getCreatedAt());
            return mapFriend;
        }).toList();
    }

    public List<Map<String, Object>> mapChallenges(List<Challenge> challengesList) {
        return challengesList.stream().map(challenge -> {
            Map<String, Object> mapChallenge = new LinkedHashMap<>();
            mapChallenge.put("title", challenge.getTitle());
            mapChallenge.put("type", challenge.getChallengeType());
            mapChallenge.put("status", challenge.getChallengeStatus());
            mapChallenge.put("createdAt", challenge.getCreatedAt());
            return mapChallenge;
        }).toList();
    }

    public Map<String, Object> mapSubscription(Subscription subscription) {
        Map<String, Object> mapSubscription = new LinkedHashMap<>();
        mapSubscription.put("plan", subscription.getPlan() != null ? subscription.getPlan().getName() : null);
        mapSubscription.put("status", subscription.getStatus());
        mapSubscription.put("startDate", subscription.getStartDate());
        mapSubscription.put("endDate", subscription.getEndDate());
        return mapSubscription;
    }

    public List<Map<String, Object>> mapTopicReviews(List<TopicReview> topicReviewList) {
        return topicReviewList.stream().map(topicReview -> {
            Map<String, Object> mapTopicReview = new LinkedHashMap<>();
            mapTopicReview.put("topic", topicReview.getTopic() != null ? topicReview.getTopic().getName() : null);
            mapTopicReview.put("rating", topicReview.getRating());
            mapTopicReview.put("comment", topicReview.getComment());
            mapTopicReview.put("status", topicReview.getStatus());
            mapTopicReview.put("createdAt", topicReview.getCreatedAt());
            mapTopicReview.put("updatedAt", topicReview.getUpdatedAt());
            return mapTopicReview;
        }).toList();
    }

    public List<Map<String, Object>> mapPayments(List<PaymentHistory> paymentHistoryList) {
        return paymentHistoryList.stream().map(paymentHistory -> {
            Map<String, Object> mapPayment = new LinkedHashMap<>();
            mapPayment.put("amount", paymentHistory.getAmount());
            mapPayment.put("currency", paymentHistory.getCurrency());
            mapPayment.put("status", paymentHistory.getPaymentStatus());
            mapPayment.put("paymentAt", paymentHistory.getPaymentAt());
            return mapPayment;
        }).toList();
    }
}
