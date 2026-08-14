package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;

/**
 * EditEventDatesByPublicIdRequest specifies what a valid request
 * to edit the start and end dates/times of an event identified
 * by its public ID should look like.
 * @param publicId the event's public id
 * @param startDateTime the event's new start date and time
 * @param endDateTime the event's new end date and time
 */
@Schema(description = "Request body for editing an event's start and end dates/times")
public record EditEventDatesByPublicIdRequest
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
        description = "The event's new start date and time, must be in the future",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "2026-10-01T10:00:00"
    )
    @Future(message = "Event start date and time must be in the future")
    @NotNull(message = "Event start date and time cannot be null")
    LocalDateTime startDateTime,

    @Schema
    (
        description = "The event's new end date and time, must be in the future and after the start time",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "2026-10-01T18:00:00"
    )
    @Future(message = "Event end date and time must be in the future")
    @NotNull(message = "Event end date and time cannot be null")
    LocalDateTime endDateTime
)
{
}