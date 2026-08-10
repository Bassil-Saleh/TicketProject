package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * EditEventDescriptionByPublicIdRequest specifies what a
 * valid request to edit the description of an event identified
 * by its public ID should look like.
 * @param publicId the event's public id
 * @param description the event's new description
 */
public record EditEventDescriptionByPublicIdRequest
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
