package com.ticketproject.webapp.dtos.responses;

import java.time.Instant;

/**
 * ErrorResponse is a DTO used to return a consistent error response body
 * when an exception is caught by the global exception handler.
 *
 * @param timestamp the time the error occurred
 * @param status    the HTTP status code
 * @param error     the HTTP status reason phrase
 * @param message   a human-readable description of the error
 * @param path      the request URI that caused the error
 */
public record ErrorResponse
(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path
)
{
}