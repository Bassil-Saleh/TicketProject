package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditEventNameByPublicIdRequest
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

    @NotBlank(message = "Event name cannot be blank")
    @Size
    (
        max = AppConstants.DTO.Events.Sizes.NAME_LENGTH,
        message = "Event name cannot be longer than " +
        AppConstants.DTO.Events.Sizes.NAME_LENGTH +
        " characters"
    )
    String name
)
{
}
