package com.ticketproject.webapp.services.email;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.constants.FrontendPaths;
import com.ticketproject.webapp.exceptions.EmailSendFailedException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
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
    private final String frontendBaseUrl;

    /**
     * Constructs a new EmailService with the required dependencies.
     * @param mailSender the mail sender for sending emails
     * @param templateEngine the Thymeleaf template engine for rendering email templates
     */
    public EmailService
    (
        JavaMailSender mailSender,
        SpringTemplateEngine templateEngine,
        @Value("${app.config.frontend-base-url}") String baseUrl
    )
    {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.frontendBaseUrl = baseUrl;
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
            // Prepare the Thymeleaf context with template variables.
            Context context = new Context();
            Map<String, Object> variables = new HashMap<>();
            variables.put("passwordResetToken", rawToken);
            variables.put("expirationHours", AppConstants.Database.PasswordResetTokens.Sizes.TOKEN_DURATION_HOURS);
            context.setVariables(variables);

            // Render the email HTML using the Thymeleaf template.
            String htmlContent = templateEngine
                .process(AppConstants.Email.Templates.PASSWORD_RESET_EMAIL, context);

            // Create and send the email.
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper
            (
                message,
                true,
                AppConstants.Email.CHARACTER_ENCODING
            );
            helper.setFrom
            (
                AppConstants.Email.FROM_ADDRESS,
                AppConstants.Project.PROJECT_NAME
            );
            helper.setTo(toEmail);
            helper.setSubject(AppConstants.Email.Subjects.PASSWORD_RESET_EMAIL);
            helper.setText(htmlContent, true);

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
            MimeMessageHelper helper = new MimeMessageHelper
            (
                message,
                true,
                AppConstants.Email.CHARACTER_ENCODING
            );
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
     * Sends a ticket email to the attendee containing a QR code
     * encoding the public token, along with event details.
     *
     * @param toEmail the attendee's email address
     * @param publicToken the public token to encode as a QR code
     * @param eventName the name of the event
     * @param startDateTime the event's start date/time
     * @param endDateTime the event's end date/time
     * @param eventHostName the full name of the event host who created the event
     * @param eventCity the city where the event takes place
     * @param eventState the state where the event takes place
     * @param eventCountry the country where the event takes place
     * @throws EmailSendFailedException if the email fails to send
     */
    public void sendTicketEmail
    (
        String toEmail,
        String publicToken,
        String eventName,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String eventHostName,
        String eventCity,
        String eventState,
        String eventCountry
    )
    {
        try
        {
            // Generate the QR code image from the public token.
            byte[] qrCodeImageBytes = generateQrCodeImage(publicToken);

            // Prepare the Thymeleaf context with template variables.
            Context context = new Context();
            Map<String, Object> variables = new HashMap<>();
            variables.put("eventName", eventName);
            variables.put("startDateTime", startDateTime.format(AppConstants.Crypto.DATE_TIME_FORMAT));
            variables.put("endDateTime", endDateTime.format(AppConstants.Crypto.DATE_TIME_FORMAT));
            variables.put("eventHostName", eventHostName);
            variables.put("eventCity", eventCity);
            variables.put("eventState", eventState);
            variables.put("eventCountry", eventCountry);
            context.setVariables(variables);

            // Render the email HTML using the Thymeleaf template.
            String htmlContent = templateEngine
                .process(AppConstants.Email.Templates.TICKET_EMAIL, context);

            // Create and send the email with the QR code as an inline attachment.
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper
            (
                message,
                true,
                AppConstants.Email.CHARACTER_ENCODING
            );
            helper.setFrom
            (
                AppConstants.Email.FROM_ADDRESS,
                AppConstants.Project.PROJECT_NAME
            );
            helper.setTo(toEmail);
            helper.setSubject(AppConstants.Email.Subjects.TICKET_EMAIL);
            helper.setText(htmlContent, true);
            helper.addInline
            (
                "qrCode",
                new jakarta.mail.util.ByteArrayDataSource(qrCodeImageBytes, "image/png")
            );

            mailSender.send(message);
        }
        catch (MailException | MessagingException | WriterException | IOException e)
        {
            throw new EmailSendFailedException
            (
                "Ticket email could not be sent. Please try again later."
            );
        }
    }

    /**
     * Generates a QR code PNG image from the given text content.
     *
     * @param content the text content to encode in the QR code
     * @return the QR code image as a byte array in PNG format
     * @throws WriterException if the QR code cannot be generated
     * @throws IOException if the image cannot be written to a byte array
     */
    private byte[] generateQrCodeImage(String content) throws WriterException, IOException
    {
        int qrCodeSize = 300;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix bitMatrix = qrCodeWriter.encode
        (
            content,
            BarcodeFormat.QR_CODE,
            qrCodeSize,
            qrCodeSize,
            hints
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Builds the full verification URL with the token as a query parameter.
     * @param rawToken the raw verification token
     * @return the full verification URL
     */
    private String buildVerificationUrl(String rawToken)
    {
        // Construct the verification URL (using the base URL from application.properties).
        String path = FrontendPaths.EventHosts.VERIFY_ACCOUNT;
        return frontendBaseUrl + path + "?token=" + rawToken;
    }
}