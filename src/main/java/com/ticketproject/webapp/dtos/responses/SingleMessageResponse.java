package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;

/**
 * SingleMessageResponse specifies what a response to a successful request
 * should look like when just responding with a single message is appropriate.
 * 
 * @param message the detail message
 */
public record SingleMessageResponse
(
    @NotBlank(message = "Message cannot be blank")
    String message
)
{
}
