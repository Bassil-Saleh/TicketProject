package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;

/**
 * VerifyEventHostResponse specifies what a valid response to
 * a request for verifying a new event host account should look like.
 * @param message message returned by the server to
 * describe the result of the request
 */
public record VerifyEventHostResponse
(
    @NotBlank(message = "Response message cannot be blank")
    String message
)
{
}
