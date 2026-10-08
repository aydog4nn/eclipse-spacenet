package org.dev.eclipse.service;

public interface MailService {
    void sendWelcomeEmail(String to, String userName);

    void sendResetPasswordEmail(String to, String resetLink);

    void sendPasswordChangedNotification(String to);

    void sendContactFormEmail(String senderName, String senderEmail, String message);
}
