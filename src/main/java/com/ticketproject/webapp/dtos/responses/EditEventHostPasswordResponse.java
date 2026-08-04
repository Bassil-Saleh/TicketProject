package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;

/**
 * EditEventHostPasswordResponse specifies what a response to a successful request
 * to change a logged in event host's password should look like.
 * @param message the detail message
 */
public record EditEventHostPasswordResponse
(
    @NotBlank(message = "Message cannot be blank")
    String message
)
{
}
