package com.ticketproject.webapp.exceptions;

/**
 * UnauthorizedException is thrown when a request requires authentication
 * but no valid JWT is provided, or the JWT is invalid, expired, or revoked.
 */
public class UnauthorizedException extends RuntimeException
{
    /**
     * Constructs a new UnauthorizedException with the specified detail message.
     * @param message the detail message
     */
    public UnauthorizedException(String message)
    {
        super(message);
    }
}