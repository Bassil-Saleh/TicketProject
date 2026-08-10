package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

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
public record EditEventDatesByPublicIdRequest
(
    @NotBlank(message = "Event public id cannot be blank")
    @Size
    (
        max = AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH,
        message = "Event public id length cannot be longer than " +
        AppConstants.Database.Events.Sizes.PUBLIC_ID_LENGTH +
        " characters"
    )
    String publicId,

    @Future(message = "Event start date and time must be in the future")
    @NotNull(message = "Event start date and time cannot be null")
    LocalDateTime startDateTime,

    @Future(message = "Event end date and time must be in the future")
    @NotNull(message = "Event end date and time cannot be null")
    LocalDateTime endDateTime
)
{
}
