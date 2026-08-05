package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;

/**
 * CreatePasswordResetTokenResponse specifies what a response to a successful request
 * to reset a password for an event host account should look like.
 * 
 * @param message the detail message
 */
public record CreatePasswordResetTokenResponse
(
    @NotBlank(message = "Response message cannot be blank")
    String message
)
{
}
