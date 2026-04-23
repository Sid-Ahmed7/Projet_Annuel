package com.glotrush.scheduler.jobs.notifications;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.glotrush.dispatcher.notifications.NotificationDispatcher;
import com.glotrush.entities.Accounts;
import com.glotrush.repositories.UserLessonProgressRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LessonReminderJob implements Job {

    @Autowired
    private UserLessonProgressRepository userLessonProgressRepository;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        log.info("=== LessonReminderJob fired at={} startOfDay={}", LocalDateTime.now(), startOfDay);

        List<Accounts> accounts = userLessonProgressRepository.findAccountsWithNoLessonToday(startOfDay);
        log.info("=== LessonReminderJob found {} account(s) with no lesson today", accounts.size());

        accounts.forEach(notificationDispatcher::sendLessonReminder);
    }

}
