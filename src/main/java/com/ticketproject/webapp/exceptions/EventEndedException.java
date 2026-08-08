package com.ticketproject.webapp.exceptions;

/**
 * EventEndedException is thrown when an attempt is made to scan
 * a ticket for an event that has already ended.
 */
public class EventEndedException extends RuntimeException
{
    /**
     * Constructs a new EventEndedException with the specified detail message.
     * @param message the detail message
     */
    public EventEndedException(String message)
    {
        super(message);
    }
}