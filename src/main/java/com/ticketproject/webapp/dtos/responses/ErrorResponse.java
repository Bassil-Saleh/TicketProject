package com.ticketproject.webapp.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Response body returned when an error occurs")
public record ErrorResponse
(
    @Schema
    (
        description = "The timestamp when the error occurred",
        example = "2026-08-14T15:30:00Z"
    )
    Instant timestamp,

    @Schema
    (
        description = "The HTTP status code of the error",
        example = "400"
    )
    int status,

    @Schema
    (
        description = "The HTTP status reason phrase",
        example = "Bad Request"
    )
    String error,

    @Schema
    (
        description = "A human-readable description of the error",
        example = "Event name cannot be blank"
    )
    String message,

    @Schema
    (
        description = "The request URI that caused the error",
        example = "/api/v1/events"
    )
    String path
)
{
}