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
     * Sends a password reset token to the specified email address.
     * 
     * @param toEmail the recipient's email address
     * @param rawToken the raw password reset token to include in the email message
     */
    public void sendPasswordResetEmail(String toEmail, String rawToken)
    {
        try
        {
            String messageBody =
                "Your password reset token is " +
                rawToken +
                "\nThe token expires in " +
                AppConstants.Database.PasswordResetTokens.Sizes.TOKEN_DURATION_HOURS +
                " hour" + 
                (AppConstants.Database.PasswordResetTokens.Sizes.TOKEN_DURATION_HOURS > 1 ? "s." : ".");
            // Create and send an email containing the password reset token.
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom
            (
                AppConstants.Email.FROM_ADDRESS,
                AppConstants.Project.PROJECT_NAME
            );
            helper.setTo(toEmail);
            helper.setSubject(AppConstants.Email.Subjects.PASSWORD_RESET_EMAIL);
            helper.setText(messageBody, false);

            mailSender.send(message);
        }
        catch (MailException | MessagingException | UnsupportedEncodingException e)
        {
            throw new EmailSendFailedException
            (
                "Password reset email could not be sent. Please try again later."
            );
        }
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
            variables.put("expirationHours", AppConstants.Database.EventHosts.Sizes.VERIFICATION_DURATION_HOURS);
            context.setVariables(variables);

            // Render the email HTML using the Thymeleaf template.
            String htmlContent = templateEngine
                .process(AppConstants.Email.Templates.VERIFICATION_EMAIL, context);

            // Create and send the email.
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom
            (
                AppConstants.Email.FROM_ADDRESS,
                AppConstants.Project.PROJECT_NAME
            );
            helper.setTo(toEmail);
            helper.setSubject(AppConstants.Email.Subjects.VERIFICATION_EMAIL);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        }
        catch (MailException | MessagingException | UnsupportedEncodingException e)
        {
            throw new EmailSendFailedException
            (
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
        // Construct the verification URL (using the base URL from application.properties).
        String baseUrl = "${app.config.base-url}";
        String path = ApiPaths.BASE + ApiPaths.EventHosts.ROOT + ApiPaths.EventHosts.VERIFICATION;
        return baseUrl + path + "?token=" + rawToken;
    }
}