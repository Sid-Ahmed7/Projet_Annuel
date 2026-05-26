package com.glotrush.scheduler.jobs.notifications;

import java.time.LocalDateTime;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.glotrush.constants.StreakConstants;
import com.glotrush.dispatcher.notifications.NotificationDispatcher;
import com.glotrush.repositories.UserLessonProgressRepository;

@Component
public class InactivityReminderJob implements Job {
    
    @Autowired
    private UserLessonProgressRepository userLessonProgressRepository;

    @Autowired
    private NotificationDispatcher notificationDispatcher;
    
     @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime now = LocalDateTime.now();
        userLessonProgressRepository.findInactiveAccounts(now.minusDays(StreakConstants.THREE_DAYS), now.minusDays(StreakConstants.SIX_DAYS))
            .forEach(a -> notificationDispatcher.sendInactivityReminder(a, StreakConstants.THREE_DAYS));
        userLessonProgressRepository.findInactiveAccounts(now.minusDays(StreakConstants.SEVEN_DAYS), now.minusDays(StreakConstants.FOURTEEN_DAYS))
            .forEach(a -> notificationDispatcher.sendInactivityReminder(a, StreakConstants.SEVEN_DAYS));
        userLessonProgressRepository.findInactiveAccounts(now.minusDays(StreakConstants.FIFTEEN_DAYS), now.minusDays(StreakConstants.THIRTY_DAYS))
            .forEach(a -> notificationDispatcher.sendInactivityReminder(a, StreakConstants.FIFTEEN_DAYS));
    }
}
