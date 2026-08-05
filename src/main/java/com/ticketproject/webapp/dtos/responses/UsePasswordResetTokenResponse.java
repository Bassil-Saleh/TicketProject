package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;

/**
 * UsePasswordResetTokenResponse specifies what a response to a
 * successful request to use a password reset token should look like.
 * 
 * @param message the detail message
 */
public record UsePasswordResetTokenResponse
(
    @NotBlank(message = "Message cannot be blank")
    String message
)
{
}
