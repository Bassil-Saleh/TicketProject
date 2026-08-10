package com.ticketproject.webapp.exceptions;

/**
 * InvalidSignatureException is thrown when an attempt to validate
 * a signature sent by a client fails, which could be for reasons
 * such as (but not necessarily limited to) the signature being
 * in an invalid format, damaged during transit, or being
 * a forged certificate.
 */
public class InvalidSignatureException extends RuntimeException
{
    /**
     * Constructs a new InvalidSignatureException with the
     * specified detail message.
     * @param message the detail message
     */
    public InvalidSignatureException(String message)
    {
        super(message);
    }
}
