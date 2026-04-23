package com.glotrush.scheduler.jobs.notifications;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.glotrush.dispatcher.notifications.NotificationDispatcher;
import com.glotrush.entities.Accounts;
import com.glotrush.repositories.UserLessonProgressRepository;

@Component
public class WeeklyGoalJob implements Job {

    @Autowired
    private UserLessonProgressRepository userLessonProgressRepository;
    @Autowired
    private NotificationDispatcher notificationDispatcher;
    @Value("${notifications.weekly-goal:5}")
    private int weeklyGoal;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime startOfWeek = LocalDate.now().minusWeeks(1)
            .with(DayOfWeek.MONDAY).atStartOfDay();
        userLessonProgressRepository.findAccountsWhoReachedWeeklyGoal(startOfWeek, weeklyGoal)
            .forEach(row -> notificationDispatcher.sendWeeklyGoalAchieved((Accounts) row[0], ((Long) row[1]).intValue()));
    }
    
}
