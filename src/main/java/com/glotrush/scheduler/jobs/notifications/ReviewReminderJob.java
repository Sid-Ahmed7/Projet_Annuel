package com.glotrush.scheduler.jobs.notifications;

import java.time.LocalDateTime;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.glotrush.dispatcher.notifications.NotificationDispatcher;
import com.glotrush.repositories.UserProgressRepository;

@Component
public class ReviewReminderJob implements Job {

    @Autowired
    private UserProgressRepository userProgressRepository;
    
    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime now = LocalDateTime.now();
        userProgressRepository
            .findAccountsWithPendingReview(now.toLocalDate().atStartOfDay(), now.minusDays(3))
            .forEach(notificationDispatcher::sendReviewReminder);
    }
    
}
