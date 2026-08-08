package com.ticketproject.webapp.exceptions;

/**
 * SigningKeyNotFoundException is thrown when the signing key
 * required to verify a scanned ticket cannot be found.
 */
public class SigningKeyNotFoundException extends RuntimeException
{
    /**
     * Constructs a new SigningKeyNotFoundException with the specified detail message.
     * @param message the detail message
     */
    public SigningKeyNotFoundException(String message)
    {
        super(message);
    }
}