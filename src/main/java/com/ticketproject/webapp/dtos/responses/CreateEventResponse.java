package com.ticketproject.webapp.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * CreateEventResponse specifies what a response to a successful request
 * to create a new event should look like.
 * 
 * @param publicId the new event's public id
 * @param message the detail message
 */
@Schema(description = "Response body returned after successfully creating a new event")
public record CreateEventResponse
(
    @Schema
    (
        description = "The unique public identifier of the newly created event",
        example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    )
    @NotBlank(message = "Event public id cannot be blank")
    String publicId,

    @Schema
    (
        description = "A human-readable message describing the result of the operation",
        example = "Event created successfully"
    )
    @NotBlank(message = "Message cannot be blank")
    String message
)
{
}