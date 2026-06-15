package com.attendance.util;

import com.sendgrid.Method;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Email sender using SendGrid API
 */
@Slf4j
@Component
public class EmailSender {
    
    private final SendGrid sendGrid;
    
    @Value("${app.mail.from:noreply@attendance-system.com}")
    private String fromEmail;
    
    public EmailSender(@Value("${app.mail.sendgrid-api-key}") String sendGridApiKey) {
        this.sendGrid = new SendGrid(sendGridApiKey);
    }
    
    /**
     * Send email via SendGrid
     */
    public boolean sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            Mail mail = new Mail(
                new Email(fromEmail),
                subject,
                new Email(toEmail),
                new Content("text/html", htmlContent)
            );
            
            Response response = sendGrid.api(
                new com.sendgrid.Request() {{
                    setMethod(Method.POST);
                    setEndpoint("mail/send");
                    setBody(mail.build());
                }}
            );
            
            boolean success = response.getStatusCode() >= 200 && response.getStatusCode() < 300;
            if (success) {
                log.info("Email sent successfully to: {}", toEmail);
            } else {
                log.error("Failed to send email to: {} - Status: {}", toEmail, response.getStatusCode());
            }
            return success;
            
        } catch (Exception e) {
            log.error("Error sending email to: {}", toEmail, e);
            return false;
        }
    }
}
