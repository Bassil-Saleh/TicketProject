package com.ticketproject.webapp.exceptions;

/**
 * EventHostUnderageException is thrown when an attempt is made to create
 * an EventHost account for a user who is under the minimum required age
 * of 18 years old.
 */
public class EventHostUnderageException extends RuntimeException
{
    /**
     * Constructs a new EventHostUnderageException with the specified
     * detail message.
     *
     * @param message the detail message
     */
    public EventHostUnderageException(String message)
    {
        super(message);
    }
}