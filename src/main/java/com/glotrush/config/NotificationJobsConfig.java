package com.glotrush.config;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.glotrush.constants.CronConstants;
import com.glotrush.scheduler.jobs.notifications.InactivityReminderJob;
import com.glotrush.scheduler.jobs.notifications.LessonReminderJob;
import com.glotrush.scheduler.jobs.notifications.ReviewReminderJob;
import com.glotrush.scheduler.jobs.notifications.StreakJob;
import com.glotrush.scheduler.jobs.notifications.WeeklyGoalJob;

@Configuration
public class NotificationJobsConfig {

    @Bean
    public JobDetail lessonReminderJobDetail() {
        return JobBuilder.newJob(LessonReminderJob.class)
                .withIdentity("lessonReminderJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger lessonReminderTrigger(JobDetail lessonReminderJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(lessonReminderJobDetail)
                .withIdentity("lessonReminderTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(CronConstants.LESSON_REMINDER_CRON))
                .build();
    }

    @Bean
    public JobDetail streakJobDetail() {
        return JobBuilder.newJob(StreakJob.class)
                .withIdentity("streakJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger streakTrigger(JobDetail streakJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(streakJobDetail)
                .withIdentity("streakTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(CronConstants.STREAK_REMINDER_CRON))
                .build();
    }

    @Bean
    public JobDetail inactivityReminderJobDetail() {
        return JobBuilder.newJob(InactivityReminderJob.class)
                .withIdentity("inactivityReminderJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger inactivityReminderTrigger(JobDetail inactivityReminderJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(inactivityReminderJobDetail)
                .withIdentity("inactivityReminderTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(CronConstants.INACTIVITY_REMINDER_CRON))
                .build();
    }

    @Bean
    public JobDetail weeklyGoalJobDetail() {
        return JobBuilder.newJob(WeeklyGoalJob.class)
                .withIdentity("weeklyGoalJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger weeklyGoalTrigger(JobDetail weeklyGoalJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(weeklyGoalJobDetail)
                .withIdentity("weeklyGoalTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(CronConstants.WEEKLY_GOAL_CRON))
                .build();
    }

    @Bean
    public JobDetail reviewReminderJobDetail() {
        return JobBuilder.newJob(ReviewReminderJob.class)
                .withIdentity("reviewReminderJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger reviewReminderTrigger(JobDetail reviewReminderJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(reviewReminderJobDetail)
                .withIdentity("reviewReminderTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(CronConstants.REVIEW_REMINDER_CRON))
                .build();
    }
}
