package com.glotrush.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
            message.setText(buildPasswordResetEmailBody(resetLink));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending password reset email to: {}", toEmail, e);
            throw new RuntimeException(messageSource.getMessage("error.email.failed_to_send", null, getCurrentLocale()), e);
        }
    }

    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.welcome", null, getCurrentLocale()));
            message.setText(buildWelcomeEmailBody(username));

            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending welcome email to: {}", toEmail, e);
        }
    }

    public void sendPasswordExpiryReminder(String toEmail, int daysRemaining) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(messageSource.getMessage("email.subject.password_expiry", null, getCurrentLocale()));
            message.setText(buildPasswordExpiryEmailBody(daysRemaining));

            mailSender.send(message);
            log.info("Password expiry reminder sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending password expiry reminder to: {}", toEmail, e);
        }
    }

    private String buildPasswordResetEmailBody(String resetLink) {
        return """
                Hello,
                
                You have requested to reset your password for your account.
                
                Click the link below to reset your password:
                %s
                
                This link will expire in 1 hour.
                
                If you did not request this password reset, please ignore this email.
                
                Best regards,
                The  Team
                """.formatted(resetLink);
    }

    private String buildWelcomeEmailBody(String username) {
        return """
                Hello %s,
                
                Welcome! 🎌
                
                We're excited to have you join our language learning community.
                Start your journey by learning your first word today!
                
                Best regards,
                The Team
                """.formatted(username != null ? username : "");
    }

    private String buildPasswordExpiryEmailBody(int daysRemaining) {
        return """
                Hello,
                
                This is a reminder that your password will expire in %d days.
                
                For security reasons, we require you to change your password every 60 days.
                Please log in to your account and update your password.
                
                Best regards,
                The Glotrush Team
                """.formatted(daysRemaining);
    }
}
 