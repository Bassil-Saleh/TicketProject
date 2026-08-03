package com.ticketproject.webapp.exceptions;

/**
 * AccountNotVerifiedException is thrown when a login attempt
 * fails because the event host's account has not yet been verified.
 */
public class AccountNotVerifiedException extends RuntimeException
{
    /**
     * Constructs a new AccountNotVerifiedException with the specified detail message.
     * @param message the detail message
     */
    public AccountNotVerifiedException(String message)
    {
        super(message);
    }
}