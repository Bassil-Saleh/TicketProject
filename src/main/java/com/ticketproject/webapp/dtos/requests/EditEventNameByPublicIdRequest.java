package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * EditEventNameByPublicIdRequest specifies what a valid request
 * to edit the name of an event identified by its public ID
 * should look like.
 * @param publicId the event's public ID
 * @param name the event's new name
 */
@Schema(description = "Request body for editing an event's name")
public record EditEventNameByPublicIdRequest
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
        description = "The event's new name",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "Updated Annual Tech Conference 2026",
        maximum = "255"
    )
    @NotBlank(message = "Event name cannot be blank")
    @Size
    (
        max = AppConstants.Database.Events.Sizes.NAME_LENGTH,
        message = "Event name cannot be longer than " +
        AppConstants.Database.Events.Sizes.NAME_LENGTH +
        " characters"
    )
    String name
)
{
}