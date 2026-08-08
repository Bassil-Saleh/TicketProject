package com.ticketproject.webapp.exceptions;

/**
 * TicketScanFailedException is thrown when a ticket scan operation
 * fails due to an unexpected error during signature verification.
 */
public class TicketScanFailedException extends RuntimeException
{
    /**
     * Constructs a new TicketScanFailedException with the specified detail message.
     * @param message the detail message
     */
    public TicketScanFailedException(String message)
    {
        super(message);
    }
}