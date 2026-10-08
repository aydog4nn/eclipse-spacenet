package org.dev.eclipse.service.impl;

import org.dev.eclipse.annotation.Audit;
import org.dev.eclipse.service.EmailTemplateService;
import org.dev.eclipse.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.Map;

@Service
public class MailServiceImpl implements MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.contact.inbox-email:${spring.mail.username}}")
    private String contactInboxEmail;

    public MailServiceImpl(JavaMailSender mailSender, EmailTemplateService emailTemplateService) {
        this.mailSender = mailSender;
        this.emailTemplateService = emailTemplateService;
    }

    @Override
    @Audit(action = "SEND_WELCOME_EMAIL", resource = "User")
    public void sendWelcomeEmail(String to, String userName) {
        sendEmail(
                to,
                "Hesabiniz hazir",
                "email/welcome",
                Map.of(
                        "userName", userName,
                        "loginUrl", frontendUrl + "/login"
                )
        );
    }

    @Override
    @Audit(action = "SEND_RESET_PASSWORD_EMAIL", resource = "User")
    public void sendResetPasswordEmail(String to, String code) {
        sendEmail(
                to,
                "Sifre yenileme kodunuz",
                "email/reset-password",
                Map.of("code", code)
        );
    }

    @Override
    @Audit(action = "SEND_PASSWORD_CHANGED_NOTIFICATION", resource = "User")
    public void sendPasswordChangedNotification(String to) {
        sendEmail(
                to,
                "Your Password Has Been Changed",
                "email/password-changed",
                Map.of()
        );
    }

    @Override
    @Audit(action = "SEND_CONTACT_FORM_EMAIL", resource = "PublicContact")
    public void sendContactFormEmail(String senderName, String senderEmail, String message) {
        sendEmail(
                senderEmail,
                contactInboxEmail,
                "Platform iletisim formu",
                "email/contact-form-notification",
                Map.of(
                        "senderName", senderName,
                        "senderEmail", senderEmail,
                        "message", message
                )
        );
    }

    private void sendEmail(String to, String subject, String templatePath, Map<String, Object> variables) {
        sendEmail(fromEmail, to, subject, templatePath, variables);
    }

    private void sendEmail(String from, String to, String subject, String templatePath, Map<String, Object> variables) {
        try {
            String content = emailTemplateService.build(templatePath, variables);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);

            logger.info("Email sent to {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}", to, e);
        }
    }
}
