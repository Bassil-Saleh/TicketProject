package com.ticketproject.webapp.exceptions;

/**
 * EventHostToVerifyNotFoundException is thrown when an attempt is made
 * to verify an EventHost account using an account verification token
 * that is not associated with any existing account.
 */
public class EventHostToVerifyNotFoundException extends RuntimeException
{
    /**
     * Constructs a new EventHostToVerifyNotFoundException with the
     * specified detail message.
     * 
     * @param message the detail message
     */
    public EventHostToVerifyNotFoundException(String message)
    {
        super(message);
    }
}
