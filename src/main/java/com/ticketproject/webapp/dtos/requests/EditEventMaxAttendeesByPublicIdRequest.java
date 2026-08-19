package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

/**
 * EditEventMaxAttendeesByPublicIdRequest specifies what a valid request
 * to change an existing event's maximum number of attendees should look like.
 * 
 * @param publicId the event's public ID
 * @param maxAttendees the new maximum number of attendees
 */
@Schema(description = "Request body for changing the max number of attendees for an existing event")
public record EditEventMaxAttendeesByPublicIdRequest
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
    String publicId,

    @Schema
    (
        description = "The maximum number of attendees allowed to register for the event",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "100",
        minimum = "1"
    )
    @Min
    (
        value = AppConstants.Database.Events.Sizes.MIN_ATTENDEES,
        message = "Max # of attendees cannot be less than 1"
    )
    @NotNull(message = "Maximum number of attendees cannot be null")
    Long maxAttendees
)
{
}
