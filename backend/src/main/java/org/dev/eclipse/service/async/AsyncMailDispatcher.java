package org.dev.eclipse.service.async;

import org.dev.eclipse.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncMailDispatcher {

    private final MailService mailService;
    private static final Logger logger = LoggerFactory.getLogger(AsyncMailDispatcher.class);

    public AsyncMailDispatcher(MailService mailService) {
        this.mailService = mailService;
    }

    @Async("mailExecutor")
    public void sendWelcomeEmail(String to, String name) {
        try {
            mailService.sendWelcomeEmail(to, name);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Async("mailExecutor")
    public void sendResetPasswordEmail(String to, String code) {
        try {
            mailService.sendResetPasswordEmail(to, code);
        } catch (Exception e) {
            logger.error("Failed to send reset password email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Async("mailExecutor")
    public void sendPasswordChangedNotification(String to) {
        try {
            mailService.sendPasswordChangedNotification(to);
        } catch (Exception e) {
            logger.error("Failed to send password changed notification to {}: {}", to, e.getMessage(), e);
        }
    }
}
