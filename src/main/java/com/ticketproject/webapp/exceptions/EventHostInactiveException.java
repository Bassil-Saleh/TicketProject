package com.ticketproject.webapp.exceptions;

/**
 * EventHostInactiveException is thrown when an attempt is made to
 * perform an action with an event host account that cannot be done
 * because said account is inactive.
 */
public class EventHostInactiveException extends RuntimeException
{
    /**
     * Constructs a new EventHostInactiveException with the
     * specified detail message.
     * 
     * @param message the detail message
     */
    public EventHostInactiveException(String message)
    {
        super(message);
    }
}
