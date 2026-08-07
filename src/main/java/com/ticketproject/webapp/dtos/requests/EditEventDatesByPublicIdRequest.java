package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;

public record EditEventDatesByPublicIdRequest
(
    @NotBlank(message = "Event public id cannot be blank")
    @Size
    (
        max = AppConstants.DTO.Events.Sizes.PUBLIC_ID_LENGTH,
        message = "Event public id length cannot be longer than " +
        AppConstants.DTO.Events.Sizes.PUBLIC_ID_LENGTH +
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
