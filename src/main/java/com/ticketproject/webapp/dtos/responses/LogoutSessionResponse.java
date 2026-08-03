package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;

/**
 * LogoutSessionResponse specifies what a valid response to a successful
 * attempt to log out of an event host account should look like.
 * @param message message returned by the server to describe
 * the result of the request (success, failure, etc.)
 */
public record LogoutSessionResponse
(
    @NotBlank(message = "Message cannot be blank")
    String message
)
{
}
