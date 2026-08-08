package com.ticketproject.webapp.exceptions;

public class InvalidSignatureException extends RuntimeException
{
    public InvalidSignatureException(String message)
    {
        super(message);
    }
}
