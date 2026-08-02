package com.ticketproject.webapp.exceptions;

/**
 * EventHostVerificationPeriodExpiredException is thrown when an attempt is made
 * to verify an unverified event host account with an expired verification token.
 */
public class EventHostVerificationPeriodExpiredException extends RuntimeException
{
    /**
     * Constructs a new EventHostVerificationPeriodExpiredException with the
     * specified detail message.
     * 
     * @param message the detail message
     */
    public EventHostVerificationPeriodExpiredException(String message)
    {
        super(message);
    }
}
