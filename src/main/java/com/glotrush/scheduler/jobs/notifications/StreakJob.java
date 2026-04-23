package com.glotrush.scheduler.jobs.notifications;

import java.time.LocalDate;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.glotrush.constants.StreakConstants;
import com.glotrush.dispatcher.notifications.NotificationDispatcher;
import com.glotrush.repositories.UserLessonProgressRepository;

@Component
public class StreakJob implements Job {

    @Autowired
    private UserLessonProgressRepository userLessonProgressRepository;

    @Autowired
    private NotificationDispatcher notificationDispatcher;
    
    @Override
    public void execute(JobExecutionContext context) {
        userLessonProgressRepository.findAccountsWithNoLessonTodayForStreak(LocalDate.now().atStartOfDay())
        .stream()
        .filter(account -> account.getCurrentStreak() >= StreakConstants.THREE_DAYS) 
        .forEach(notificationDispatcher::sendStreakUrgency);

    }
}
