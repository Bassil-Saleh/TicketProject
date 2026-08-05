package com.ticketproject.webapp.exceptions;

/**
 * InvalidRequestException is thrown when a request fails due to
 * being incorrectly formatted (i.e. creating an event with a precise
 * location, but leaving out latitude or longitude coordinates).
 */
public class InvalidRequestException extends RuntimeException
{
    /**
     * Constructs a new InvalidRequestException with the specified detail message.
     * @param message the detail message
     */
    public InvalidRequestException(String message)
    {
        super(message);
    }
}
