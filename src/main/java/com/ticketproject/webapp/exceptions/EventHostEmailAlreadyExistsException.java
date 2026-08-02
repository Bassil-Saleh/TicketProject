package com.ticketproject.webapp.exceptions;

/**
 * EventHostEmailAlreadyExistsException is thrown when an attempt is made
 * to create an EventHost account using an email address that is already
 * associated with an existing account.
 */
public class EventHostEmailAlreadyExistsException extends RuntimeException
{
    /**
     * Constructs a new EventHostEmailAlreadyExistsException with the
     * specified detail message.
     *
     * @param message the detail message
     */
    public EventHostEmailAlreadyExistsException(String message)
    {
        super(message);
    }
}