package com.ticketproject.webapp.exceptions;

/**
 * EmailAlreadyExistsException is thrown when an attempt is made
 * to perform an action with an entity that cannot be done due to
 * said entity having an email address associated with a preexisting
 * entity in the database (i.e. creating an event host account using
 * an email address that is already associated with an existing account).
 */
public class EmailAlreadyExistsException extends RuntimeException
{
    /**
     * Constructs a new EmailAlreadyExistsException with the
     * specified detail message.
     *
     * @param message the detail message
     */
    public EmailAlreadyExistsException(String message)
    {
        super(message);
    }
}