package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditEventDescriptionByPublicIdRequest
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

    @NotBlank(message = "Event description cannot be blank")
    @Size
    (
        max = AppConstants.DTO.Events.Sizes.DESCRIPTION_LENGTH,
        message = "Event description cannot be longer than " +
        AppConstants.DTO.Events.Sizes.DESCRIPTION_LENGTH +
        " characters"
    )
    String description
)
{
}
