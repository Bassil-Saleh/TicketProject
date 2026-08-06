package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;

/**
 * DeleteEventByPublicIdResponse specifies what a response to a successful request
 * to delete an event created by the logged in event host should look like.
 * 
 * @param message the detail message
 */
public record DeleteEventByPublicIdResponse
(
    @NotBlank(message = "Message cannot be null")
    String message
)
{
}
