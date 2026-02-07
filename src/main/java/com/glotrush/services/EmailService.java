package com.glotrush.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.glotrush.exceptions.EmailSendException;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final MessageSource messageSource;

    protected final Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.password_reset", null, getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.password_reset", new Object[]{resetLink}, getCurrentLocale()));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending password reset email to: {}", toEmail, e);
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, getCurrentLocale()), e);
        }
    }

    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.welcome", null, getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.welcome", new Object[]{username != null ? username : ""}, getCurrentLocale()));

            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending welcome email to: {}", toEmail, e);
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, getCurrentLocale()), e);
        }
    }

    public void sendPasswordExpiryReminder(String toEmail, int daysRemaining) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.password_expiry", null, getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.password_expiry", new Object[]{daysRemaining}, getCurrentLocale()));

            mailSender.send(message);
            log.info("Password expiry reminder sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending password expiry reminder to: {}", toEmail, e);
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, getCurrentLocale()), e);
        }
    }

    public void  sendPremiumUpgratedEmail(String toEmail, String username, LocalDateTime endDate){
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.subscription_upgraded", null,getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.subscription_upgraded", new Object[]{username, endDate}, getCurrentLocale()));
            mailSender.send(message);
        } catch(Exception e) {
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, getCurrentLocale()), e);
        }
    }
    public void sendSubscriptionExpiredSoonEmail(String toEmail, String username, long daysRemaining) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.subscription_expiry_reminder", null,getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.subscription_expiry_reminder", new Object[]{username, daysRemaining}, getCurrentLocale()));
            mailSender.send(message);
        } catch(Exception e) {
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, getCurrentLocale()), e);
        }
    }

    public void sendSubscriptionExpiredEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.subscription_expired", null,getCurrentLocale()));
            message.setText(messageSource.getMessage("email.body.subscription_expired", new Object[]{username}, getCurrentLocale()));
            mailSender.send(message);
        } catch(Exception e) {
            throw new EmailSendException(messageSource.getMessage("error.email.failed_to_send", null, getCurrentLocale()), e);
        }

    }


}
 