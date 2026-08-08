package com.ticketproject.webapp.exceptions;

/**
 * TicketGenerationException is thrown when a ticket cannot be generated
 * due to issues with the cryptographic signing process, such as an
 * unsupported algorithm, invalid private key, or uninitialized generator.
 */
public class TicketGenerationException extends RuntimeException
{
    /**
     * Constructs a new TicketGenerationException with the specified detail message.
     * @param message the detail message
     */
    public TicketGenerationException(String message)
    {
        super(message);
    }
}