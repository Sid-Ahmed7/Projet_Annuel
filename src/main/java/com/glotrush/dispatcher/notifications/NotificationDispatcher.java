package com.glotrush.dispatcher.notifications;

import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
    
}
