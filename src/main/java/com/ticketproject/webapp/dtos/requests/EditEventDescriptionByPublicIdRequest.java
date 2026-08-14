package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * EditEventDescriptionByPublicIdRequest specifies what a
 * valid request to edit the description of an event identified
 * by its public ID should look like.
 * @param publicId the event's public id
 * @param description the event's new description
 */
@Schema(description = "Request body for editing an event's description")
public record EditEventDescriptionByPublicIdRequest
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
        description = "The event's new description",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "An updated description for the event with new details.",
        maximum = "5000"
    )
    @NotBlank(message = "Event description cannot be blank")
    @Size
    (
        max = AppConstants.Database.Events.Sizes.DESCRIPTION_LENGTH,
        message = "Event description cannot be longer than " +
        AppConstants.Database.Events.Sizes.DESCRIPTION_LENGTH +
        " characters"
    )
    String description
)
{
}