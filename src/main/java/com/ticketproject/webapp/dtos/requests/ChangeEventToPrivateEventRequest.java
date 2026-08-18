package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ChangeEventToPrivateEventRequest specifies what a valid request
 * to change an existing event into a private one should look like.
 * 
 * @param publicId the event's public ID
 */
@Schema(description = "Request body for changing an event into a private event")
public record ChangeEventToPrivateEventRequest
(
    @Schema
    (
        description = "The public ID of the event to edit",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        maximum = "36"
    )
    @NotBlank(message = "Event public id cannot be blank")
    @Size
    (
        max = AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH,
        message = "Event public id length cannot be longer than " +
        AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH +
        " characters"
    )
    String publicId
)
{
}
