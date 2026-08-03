package com.ticketproject.webapp.exceptions;

/**
 * InvalidCredentialsException is thrown when a login attempt
 * fails due to an incorrect email address or password.
 */
public class InvalidCredentialsException extends RuntimeException
{
    /**
     * Constructs a new InvalidCredentialsException with the specified detail message.
     * @param message the detail message
     */
    public InvalidCredentialsException(String message)
    {
        super(message);
    }
}