package com.ticketproject.webapp.dtos.responses;

/**
 * CreateEventHostResponse specifies what a valid response to a request
 * for creating a new event host account should look like.
 * @param message message returned by the server to describe
 * the result of the request (success, failure, etc.)
 */
public record CreateEventHostResponse
(
    String message
) {}
