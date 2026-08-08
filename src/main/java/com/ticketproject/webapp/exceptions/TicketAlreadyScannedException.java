package com.ticketproject.webapp.exceptions;

/**
 * TicketAlreadyScannedException is thrown when an attempt is made
 * to scan a ticket that has already been marked as present.
 */
public class TicketAlreadyScannedException extends RuntimeException
{
    /**
     * Constructs a new TicketAlreadyScannedException with the specified detail message.
     * @param message the detail message
     */
    public TicketAlreadyScannedException(String message)
    {
        super(message);
    }
}