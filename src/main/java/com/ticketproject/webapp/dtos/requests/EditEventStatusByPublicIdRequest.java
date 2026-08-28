package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.enums.EventStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * EditEventStatusByPublicIdRequest specifies what a
 * valid request to edit an event's status should look like.
 * Note that only the event host who created the event
 * should be allowed to edit it.
 * @param publicId the event's public ID
 * @param status the event's new status
 */
@Schema(description = "Request body for changing an existing event's status")
public record EditEventStatusByPublicIdRequest
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
        description = "The new status of the event: PUBLISHED or CANCELED",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "PUBLISHED"
    )
    @NotNull(message = "Event status cannot be null")
    EventStatus status
)
{
}
