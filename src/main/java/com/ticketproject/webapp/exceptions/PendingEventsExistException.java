package com.ticketproject.webapp.exceptions;

/**
 * PendingEventsException is thrown when an attempt to
 * service a request fails because there is at least
 * one event by the logged in event host which has not ended yet.
 */
public class PendingEventsExistException extends RuntimeException
{
    /**
     * Constructs a new PendingEventsExistException with the specified detail message.
     * @param message the detail message
     */
    public PendingEventsExistException(String message)
    {
        super(message);
    }
}
