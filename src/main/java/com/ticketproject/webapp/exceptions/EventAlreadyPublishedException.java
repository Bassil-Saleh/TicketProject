package com.ticketproject.webapp.exceptions;

/**
 * EventAlreadyPublishedException is thrown when an attempt is made
 * to perform an action on an Event entity that should not be done
 * due to the Event already being published.
 */
public class EventAlreadyPublishedException extends RuntimeException
{
    /**
     * Constructs a new EventAlreadyPublishedException with the
     * specified detail message.
     * 
     * @param message the detail message
     */
    public EventAlreadyPublishedException(String message)
    {
        super(message);
    }
}
