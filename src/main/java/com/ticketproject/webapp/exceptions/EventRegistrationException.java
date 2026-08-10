package com.ticketproject.webapp.exceptions;

/**
 * EventRegistrationException is thrown when an attempt is made to
 * register for an event in a situation where the registration
 * cannot or should not be done (i.e. attempting to register for
 * an event at maximum capacity).
 */
public class EventRegistrationException extends RuntimeException
{
    /**
     * Constructs a new EventRegistrationException with the
     * specified detail message.
     * @param message the detail message
     */
    public EventRegistrationException(String message)
    {
        super(message);
    }
}
