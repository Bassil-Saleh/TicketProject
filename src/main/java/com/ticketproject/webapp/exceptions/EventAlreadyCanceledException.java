package com.ticketproject.webapp.exceptions;

/**
 * EventAlreadyCanceledException is thrown when an attempt is made
 * to perform an action on an Event entity that should not be done
 * due to the Event already being canceled.
 */
public class EventAlreadyCanceledException extends RuntimeException
{
    /**
     * Constructs a new EventAlreadyCanceledException with
     * the specified detail message.
     * @param message the detail message
     */
    public EventAlreadyCanceledException(String message)
    {
        super(message);
    }
}
