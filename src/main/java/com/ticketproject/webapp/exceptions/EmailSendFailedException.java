package com.ticketproject.webapp.exceptions;

/**
 * EmailSendFailedException is thrown when the application fails
 * to send an email (e.g., an account verification email) to the user.
 */
public class EmailSendFailedException extends RuntimeException
{
    /**
     * Constructs a new EmailSendFailedException with the
     * specified detail message.
     *
     * @param message the detail message
     */
    public EmailSendFailedException(String message)
    {
        super(message);
    }
}