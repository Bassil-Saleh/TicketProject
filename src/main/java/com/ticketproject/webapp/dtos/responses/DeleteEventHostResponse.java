package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;

/**
 * DeleteEventHostResponse specifies what a response to a successful request
 * to delete an event host account should look like.
 * 
 * @param message the detail message
 */
public record DeleteEventHostResponse
(
    @NotBlank(message = "Response message cannot be blank")
    String message
)
{
}
