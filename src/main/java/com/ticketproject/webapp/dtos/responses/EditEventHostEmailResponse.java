package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;

/**
 * EditEventHostEmailResponse specifies what a response to a successful request
 * to change a logged in event host's email address should look like.
 * 
 * @param message the detail message
 */
public record EditEventHostEmailResponse
(
    @NotBlank(message = "Message cannot be blank")
    String message
)
{
}
