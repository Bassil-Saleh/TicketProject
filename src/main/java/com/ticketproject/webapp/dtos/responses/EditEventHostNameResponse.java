package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;

/**
 * EditEventHostNameResponse specifies what a response to a successful request
 * to change a logged in event host's name should look like.
 * 
 * @param message the detail message
 */
public record EditEventHostNameResponse
(
    @NotBlank(message = "Message cannot be blank")
    String message
)
{
}
