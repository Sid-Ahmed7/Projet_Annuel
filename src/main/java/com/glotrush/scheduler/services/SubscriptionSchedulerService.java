package com.glotrush.scheduler.services;

import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

import com.glotrush.entities.Subscription;
import com.glotrush.exceptions.SchedulerQuartzException;
import com.glotrush.scheduler.subscription.SubscriptionExpirationJob;
import com.glotrush.scheduler.subscription.SubscriptionReminderJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionSchedulerService implements ISubscriptionSchedulerService {
 
    private final Scheduler scheduler;
    private static final String JOB_GROUP = "subscription-expiration";
    private static final String TRIGGER_GROUP = "subscription-expiration-triggers";
    private static final String JOB_GROUP_REMINDER = "subscription-reminder";
    private static final String TRIGGER_GROUP_REMINDER = "subscription-reminder-triggers";
    private static final int REMINDER_HOUR_BEFORE_EXPIRATION = 24;


    @Override
    public void scheduleExpiration(Subscription subscription) {
        if(subscription.getCurrentPeriodEnd() == null) {
            return;
        }
        
        cancelExpirationSchedule(subscription.getId());

        if(subscription.getCurrentPeriodEnd().isBefore(LocalDateTime.now())) {
            return;
        }

        JobDetail jobDetail = buildJobDetail(subscription.getId());
        Trigger trigger = buildTrigger(subscription.getId(), subscription.getCurrentPeriodEnd());

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new SchedulerQuartzException("Error scheduling subscription expiration", e);
        }
    }
    @Override
    public boolean isExpirationScheduled(UUID subscriptionId) {
        try {
            JobKey jobKey = new JobKey(subscriptionId.toString(), JOB_GROUP);
            return scheduler.checkExists(jobKey);
        } catch (SchedulerException e) {
            return false;
        }
    }


    @Override
    public void cancelExpirationSchedule(UUID subscriptionId) {
        deleteJobIfExists(subscriptionId, JOB_GROUP);       
    }

    @Override
    public void cancelReminderSchedule(UUID subscriptionId) {
        deleteJobIfExists(subscriptionId, JOB_GROUP_REMINDER);
    }

    @Override
    public void cancelAllSchedulesForSubscription(UUID subscriptionId) {
        cancelExpirationSchedule(subscriptionId);
        cancelReminderSchedule(subscriptionId);
    }

    @Override
    public void scheduleReminder(Subscription subscription) {
        if(subscription.getCurrentPeriodEnd() == null) {
            return;
        }
        
        cancelReminderSchedule(subscription.getId());

        LocalDateTime reminderTime = subscription.getCurrentPeriodEnd().minusHours(REMINDER_HOUR_BEFORE_EXPIRATION);

        if(reminderTime.isBefore(LocalDateTime.now())) {
            return;
        }

        JobDetail jobDetail = buildReminderJobDetail(subscription.getId());
        Trigger trigger = buildReminderTrigger(subscription.getId(), reminderTime);

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new SchedulerQuartzException("Error scheduling subscription reminder", e);
        }
    }

    private JobDetail buildJobDetail(UUID subscriptionId) {
        return JobBuilder.newJob(SubscriptionExpirationJob.class)
                .withIdentity(subscriptionId.toString(), JOB_GROUP)
                .usingJobData("subscriptionId", subscriptionId.toString())
                .build();
    }

    private JobDetail buildReminderJobDetail(UUID subscriptionId) {
        return JobBuilder.newJob(SubscriptionReminderJob.class)
                .withIdentity(subscriptionId.toString(), JOB_GROUP_REMINDER)
                .usingJobData("subscriptionId", subscriptionId.toString())
                .build();
    }

    private Trigger buildTrigger(UUID subscriptionId, LocalDateTime endDate) {
                Date triggerDate = Date.from(endDate.atZone(ZoneId.systemDefault()).toInstant());
        return TriggerBuilder.newTrigger()
                .withIdentity(subscriptionId.toString(), TRIGGER_GROUP)
                .startAt(triggerDate)
                .build();
    }

    private Trigger buildReminderTrigger(UUID subscriptionId, LocalDateTime reminderTime) {
                Date triggerDate = Date.from(reminderTime.atZone(ZoneId.systemDefault()).toInstant());
        return TriggerBuilder.newTrigger()
                .withIdentity(subscriptionId.toString(), TRIGGER_GROUP_REMINDER)
                .startAt(triggerDate)
                .build();
    }

    private void deleteJobIfExists(UUID subscriptionId, String jobGroup) {
        try {
            JobKey jobKey = new JobKey(subscriptionId.toString(), jobGroup);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            throw new SchedulerQuartzException("Error cancelling subscription schedule", e);
        }
    }
}