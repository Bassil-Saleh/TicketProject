package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * EditEventNameByPublicIdRequest specifies what a valid request
 * to edit the name of an event identified by its public ID
 * should look like.
 * @param publicId the event's public ID
 * @param name the event's new name
 */
public record EditEventNameByPublicIdRequest
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
