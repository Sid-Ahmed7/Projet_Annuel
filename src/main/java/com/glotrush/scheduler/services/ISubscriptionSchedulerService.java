package com.glotrush.scheduler.services;

import java.util.UUID;

import com.glotrush.entities.Subscription;

public interface ISubscriptionSchedulerService {

        void scheduleExpiration(Subscription subscription);
        void cancelExpirationSchedule(UUID subscriptionId);
        boolean isExpirationScheduled(UUID subscriptionId);
        void scheduleReminder(Subscription subscription);
        void cancelReminderSchedule(UUID subscriptionId);
        void cancelAllSchedulesForSubscription(UUID subscriptionId);
}
