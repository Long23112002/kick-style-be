package org.longg.nh.kickstyleecommerce.domain.services;

public interface MailService {
    
    /**
     * Send a simple text email
     * @param to recipient email address
     * @param subject email subject
     * @param text email content
     */
    void sendSimpleMessage(String to, String subject, String text);
    
    /**
     * Send an HTML email
     * @param to recipient email address
     * @param subject email subject
     * @param htmlContent HTML email content
     */
    void sendHtmlMessage(String to, String subject, String htmlContent);
    
    /**
     * Send verification email
     * @param to recipient email address
     * @param verificationToken verification token
     */
    void sendVerificationEmail(String to, String verificationToken);
} 