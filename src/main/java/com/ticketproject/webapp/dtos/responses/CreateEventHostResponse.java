package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;

/**
 * CreateEventHostResponse specifies what a valid response to a request
 * for creating a new event host account should look like.
 * 
 * @param message message returned by the server to describe
 * the result of the request (success, failure, etc.)
 */
public record CreateEventHostResponse
(
    @NotBlank(message = "Response message cannot be blank")
    String message
) {}