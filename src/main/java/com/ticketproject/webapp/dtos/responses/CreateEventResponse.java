package com.ticketproject.webapp.dtos.responses;

import jakarta.validation.constraints.NotBlank;

/**
 * CreateEventResponse specifies what a response to a successful request
 * to create a new event should look like.
 * 
 * @param publicId the new event's public id
 * @param message the detail message
 */
public record CreateEventResponse
(
    @NotBlank(message = "Event public id cannot be blank")
    String publicId,

    @NotBlank(message = "Message cannot be blank")
    String message
)
{
}
