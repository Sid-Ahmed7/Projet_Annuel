package com.glotrush.scheduler.subscription;

import java.util.UUID;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.glotrush.services.subscription.ISubscriptionService;

@Component
public class SubscriptionReminderJob implements Job{
    
    @Autowired
    private ISubscriptionService subscriptionService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        UUID subscriptionId = UUID.fromString(context.getJobDetail().getJobDataMap().getString("subscriptionId"));
        try {
            subscriptionService.sendReminderEmailForExpiringSubscription(subscriptionId);
        } catch (Exception e) {
            throw new JobExecutionException("Error while sending subscription reminder", e);
        }
    }
}
