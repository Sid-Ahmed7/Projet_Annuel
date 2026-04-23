package com.glotrush.dispatcher.notifications;

import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.glotrush.constants.StreakConstants;
import com.glotrush.entities.Accounts;
import com.glotrush.entities.Lesson;
import com.glotrush.entities.Topic;
import com.glotrush.entities.UserLanguage;
import com.glotrush.enumerations.LanguageType;
import com.glotrush.repositories.UserLanguageRepository;
import com.glotrush.services.EmailService;
import com.glotrush.services.pushNotifications.IPushNotification;
import com.glotrush.utils.LocaleUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationDispatcher {
    private final UserLanguageRepository userLanguageRepository;
    private final EmailService emailService;
    private final IPushNotification pushNotificationService;
    private final MessageSource messageSource;


    @Async
    @Transactional
    public void sendNotificationWhenNewLesson(Lesson lesson) {
        UUID languageId = lesson.getTopic().getLanguage().getId();
        String languageName = lesson.getTopic().getLanguage().getName();
        String topicName = lesson.getTopic().getName();
        String lessonTitle = lesson.getTitle();

        List<UserLanguage> userLanguages = userLanguageRepository.findByLanguage_IdAndLanguageType(languageId, LanguageType.LEARNING);

        for(UserLanguage userLanguage : userLanguages) {
            Accounts account = userLanguage.getAccount();
            String fullName = account.getFirstName() + " " + account.getLastName();
            String userName = account.getProfile() != null ? account.getUsername() : fullName;
            
            emailService.sendNewLessonEmail(account.getEmail(), userName, lessonTitle, topicName, languageName);
            String title = messageSource.getMessage("push.title.new_lesson", null, LocaleUtils.getCurrentLocale());
            String body = messageSource.getMessage("push.body.new_lesson", new Object[]{lessonTitle, topicName}, LocaleUtils.getCurrentLocale());
            pushNotificationService.sendNotification(account.getId(), title, body);
        }

    }

    @Async
    @Transactional
    public void sendNotificationWhenNewTopic(Topic topic) {
        UUID languageId = topic.getLanguage().getId();
        String languageName = topic.getLanguage().getName();
        String topicName = topic.getName();
        String difficulty = topic.getDifficulty().name();

        List<UserLanguage> userLanguages = userLanguageRepository.findByLanguage_IdAndLanguageType(languageId, LanguageType.LEARNING);

        for(UserLanguage userLanguage : userLanguages) {
            Accounts account = userLanguage.getAccount();
            String fullName = account.getFirstName() + " " + account.getLastName();
            String userName = account.getProfile() != null ? account.getUsername() : fullName;
            
            emailService.sendNewTopicEmail(account.getEmail(), userName, topicName, languageName, difficulty);
            String title = messageSource.getMessage("push.title.new_topic", null, LocaleUtils.getCurrentLocale());
            String body = messageSource.getMessage("push.body.new_topic", new Object[]{topicName, languageName, difficulty}, LocaleUtils.getCurrentLocale());
            pushNotificationService.sendNotification(account.getId(), title, body);

        }
    }

    @Async
    @Transactional
    public void sendLessonReminder(Accounts account) {
        log.info("=== sendLessonReminder called for accountId={} firstName={} streak={}", account.getId(), account.getFirstName(), account.getCurrentStreak());
        int streak = account.getCurrentStreak();
        String messageKey = streak > 0 ? "notif.lesson.reminder.streak.body" : "notif.lesson.reminder.body";
        String title = messageSource.getMessage("notif.lesson.reminder.title", null, LocaleUtils.getCurrentLocale());
        String body = messageSource.getMessage(messageKey, new Object[]{account.getFirstName(), streak}, LocaleUtils.getCurrentLocale());
        log.info("=== sendLessonReminder -> title='{}' body='{}'", title, body);
        pushNotificationService.sendNotification(account.getId(), title, body);
    }
    @Async
    @Transactional
    public void sendStreakUrgency(Accounts account) {
        if (!account.isNotifStreakUrgency()){
            return;
        }
        String title = messageSource.getMessage("notif.streak.urgency.title", null, LocaleUtils.getCurrentLocale());
        String body = messageSource.getMessage("notif.streak.urgency.body", new Object[]{account.getFirstName(), account.getCurrentStreak()}, LocaleUtils.getCurrentLocale());
        pushNotificationService.sendNotification(account.getId(), title, body);
    }

    @Async
    @Transactional
    public void sendInactivityReminder(Accounts account, int days) {
        String message = account.getLongestStreak() > 0 ? "notif.inactivity.streak.body" : "notif.inactivity.body";
        String title = messageSource.getMessage("notif.inactivity.title", null, LocaleUtils.getCurrentLocale());
        String body = messageSource.getMessage(message, new Object[]{account.getFirstName(), days, account.getLongestStreak()}, LocaleUtils.getCurrentLocale());

        if( days <= StreakConstants.SEVEN_DAYS) {
            pushNotificationService.sendNotification(account.getId(), title, body);
            
            if(days == StreakConstants.SEVEN_DAYS) {
                emailService.sendNotificationEmail(account.getEmail(), title, body);
            }
        } else {
            emailService.sendNotificationEmail(account.getEmail(), title, body);
        }
    }

    @Async
    @Transactional
    public void sendWeeklyGoalAchieved(Accounts account, int lessonsCompleted) {
        String title = messageSource.getMessage("notif.weekly.goal.title", null, LocaleUtils.getCurrentLocale());
        String body = messageSource.getMessage("notif.weekly.goal.body", new Object[]{account.getFirstName(), lessonsCompleted}, LocaleUtils.getCurrentLocale());
        pushNotificationService.sendNotification(account.getId(), title, body);
        emailService.sendNotificationEmail(account.getEmail(), title, body);
    }

    @Async
    @Transactional
    public void sendReviewReminder(Accounts account) {
        String title = messageSource.getMessage("notif.review.title", null, LocaleUtils.getCurrentLocale());
        String body = messageSource.getMessage("notif.review.body", new Object[]{account.getFirstName()}, LocaleUtils.getCurrentLocale());
        pushNotificationService.sendNotification(account.getId(), title, body);
    }

    @Async
    @Transactional
    public void sendStreakMilestone(Accounts account, int streak) {
        String title = messageSource.getMessage("notif.streak.milestone.title", null, LocaleUtils.getCurrentLocale());
        String body = messageSource.getMessage("notif.streak.milestone.body", new Object[]{account.getFirstName(), streak}, LocaleUtils.getCurrentLocale());
        pushNotificationService.sendNotification(account.getId(), title, body);
        String emailSubject = messageSource.getMessage("email.subject.streak.milestone", new Object[]{streak}, LocaleUtils.getCurrentLocale());
        String emailBody = messageSource.getMessage("email.body.streak.milestone", new Object[]{account.getFirstName(), streak}, LocaleUtils.getCurrentLocale());
        emailService.sendNotificationEmail(account.getEmail(), emailSubject, emailBody);
    }



    
}
