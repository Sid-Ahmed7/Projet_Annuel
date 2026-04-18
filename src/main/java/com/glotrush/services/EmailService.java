package com.glotrush.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.glotrush.exceptions.EmailSendException;
import com.glotrush.utils.LocaleUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final MessageSource messageSource;


    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.password_reset", null, LocaleUtils.getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.password_reset", new Object[]{resetLink}, LocaleUtils.getCurrentLocale()));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending password reset email to: {}", toEmail, e);
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, LocaleUtils.getCurrentLocale()), e);
        }
    }

    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.welcome", null, LocaleUtils.getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.welcome", new Object[]{username != null ? username : ""}, LocaleUtils.getCurrentLocale()));

            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending welcome email to: {}", toEmail, e);
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, LocaleUtils.getCurrentLocale()), e);
        }
    }

    public void sendPasswordExpiryReminder(String toEmail, int daysRemaining) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.password_expiry", null, LocaleUtils.getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.password_expiry", new Object[]{daysRemaining}, LocaleUtils.getCurrentLocale()));

            mailSender.send(message);
            log.info("Password expiry reminder sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending password expiry reminder to: {}", toEmail, e);
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, LocaleUtils.getCurrentLocale()), e);
        }
    }

    public void  sendPremiumUpgratedEmail(String toEmail, String username, LocalDateTime endDate){
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.subscription_upgraded", null, LocaleUtils.getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.subscription_upgraded", new Object[]{username, endDate}, LocaleUtils.getCurrentLocale()));
            mailSender.send(message);
        } catch(Exception e) {
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, LocaleUtils.getCurrentLocale()), e);
        }
    }
    public void sendSubscriptionExpiredSoonEmail(String toEmail, String username, long daysRemaining) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.subscription_expiry_reminder", null, LocaleUtils.getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.subscription_expiry_reminder", new Object[]{username, daysRemaining}, LocaleUtils.getCurrentLocale()));
            mailSender.send(message);
        } catch(Exception e) {
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, LocaleUtils.getCurrentLocale()), e);
        }
    }

    public void sendSubscriptionExpiredEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.subscription_expired", null, LocaleUtils.getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.subscription_expired", new Object[]{username}, LocaleUtils.getCurrentLocale()));
            mailSender.send(message);
        } catch(Exception e) {
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, LocaleUtils.getCurrentLocale()), e);
        }
    }

    public void sendSubscriptionCancellationEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.subscription.cancelled", null, LocaleUtils.getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.subscription.cancelled", new Object[]{username}, LocaleUtils.getCurrentLocale()));
            mailSender.send(message);
        } catch(Exception e) {
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, LocaleUtils.getCurrentLocale()), e);
        }
    }

    public void sendReviewApprovedEmail(String toEmail, String username, String topicName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.review_approved", null, LocaleUtils.getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.review_approved", new Object[]{username, topicName}, LocaleUtils.getCurrentLocale()));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error sending review approved email to: {}", toEmail, e);
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, LocaleUtils.getCurrentLocale()), e);        }
    }

    public void sendReviewRejectedEmail(String toEmail, String username, String topicName, int rejectedCount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.review_rejected", null, LocaleUtils.getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.review_rejected", new Object[]{username, topicName, rejectedCount}, LocaleUtils.getCurrentLocale()));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error sending review rejected email to: {}", toEmail, e);
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, LocaleUtils.getCurrentLocale()), e);
        }
    }

    public void sendReviewBannedEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.review_banned", null, LocaleUtils.getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.review_banned", new Object[]{username}, LocaleUtils.getCurrentLocale()));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error sending review banned email to: {}", toEmail, e);
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, LocaleUtils.getCurrentLocale()), e);
        }
    }


}
 