package com.ticketproject.webapp.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * SingleMessageResponse specifies what a response to a successful request
 * should look like when just responding with a single message is appropriate.
 * 
 * @param message the detail message
 */
@Schema(description = "Response body containing a single message indicating the result of the operation")
public record SingleMessageResponse
(
    @Schema
    (
        description = "A human-readable message describing the result of the operation",
        example = "Operation completed successfully"
    )
    @NotBlank(message = "Message cannot be blank")
    String message
)
{
}