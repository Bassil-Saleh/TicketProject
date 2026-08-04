package com.ticketproject.webapp.services;

import com.ticketproject.webapp.constants.ApiPaths;
import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.exceptions.EmailSendFailedException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * EmailService is a service used to send emails to users,
 * such as account verification emails.
 */
@Service
public class EmailService
{
    private static final String FROM_ADDRESS = "noreply@ticketproject.local";
    private static final String FROM_NAME = "TicketProject";
    private static final String VERIFICATION_EMAIL_TEMPLATE = "email/verification-email";
    private static final String VERIFICATION_EMAIL_SUBJECT = "Verify Your TicketProject Account";

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    /**
     * Constructs a new EmailService with the required dependencies.
     * @param mailSender the mail sender for sending emails
     * @param templateEngine the Thymeleaf template engine for rendering email templates
     */
    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine)
    {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Sends an account verification email to the specified email address.
     * The email contains a link with the verification token that the user
     * can click to verify their account.
     *
     * @param toEmail the recipient's email address
     * @param rawToken the raw verification token to include in the verification link
     * @throws EmailSendFailedException if the email fails to send
     */
    public void sendVerificationEmail(String toEmail, String rawToken)
    {
        try
        {
            // Build the verification URL.
            String verificationUrl = buildVerificationUrl(rawToken);

            // Prepare the Thymeleaf context with template variables.
            Context context = new Context();
            Map<String, Object> variables = new HashMap<>();
            variables.put("verificationUrl", verificationUrl);
            variables.put("expirationHours", AppConstants.DTO.EventHosts.Sizes.VERIFICATION_LINK_DURATION_HOURS);
            context.setVariables(variables);

            // Render the email HTML using the Thymeleaf template.
            String htmlContent = templateEngine.process(VERIFICATION_EMAIL_TEMPLATE, context);

            // Create and send the email.
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(FROM_ADDRESS, FROM_NAME);
            helper.setTo(toEmail);
            helper.setSubject(VERIFICATION_EMAIL_SUBJECT);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        }
        catch (MailException | MessagingException | UnsupportedEncodingException e)
        {
            throw new EmailSendFailedException(
                "Account verification email could not be sent. Please try creating a new account later."
            );
        }
    }

    /**
     * Builds the full verification URL with the token as a query parameter.
     * @param rawToken the raw verification token
     * @return the full verification URL
     */
    private String buildVerificationUrl(String rawToken)
    {
        // Construct the base URL (assuming localhost:8080 for local development).
        // In production, this would be configured via application properties.
        String baseUrl = "http://localhost:8080";
        String path = ApiPaths.BASE + ApiPaths.EventHosts.ROOT + ApiPaths.EventHosts.VERIFICATION;
        return baseUrl + path + "?token=" + rawToken;
    }
}