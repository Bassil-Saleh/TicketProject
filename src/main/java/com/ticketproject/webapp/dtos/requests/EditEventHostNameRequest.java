package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * EditEventHostNameRequest specifies what a valid request to
 * edit the logged in event host's name should look like.
 * 
 * @param firstName first name
 * @param middleName middle name (can be null)
 * @param lastName last name
 */
public record EditEventHostNameRequest
(
    @NotBlank(message = "First name is required")
    @Size
    (
        max = AppConstants.DTO.EventHosts.Sizes.MAX_NAME_LENGTH,
        message = "First name must not exceed " + AppConstants.DTO.EventHosts.Sizes.MAX_NAME_LENGTH + " characters"
    )
    String firstName,

    @Size
    (
        max = AppConstants.DTO.EventHosts.Sizes.MAX_NAME_LENGTH,
        message = "Middle name must not exceed " + AppConstants.DTO.EventHosts.Sizes.MAX_NAME_LENGTH + " characters"
    )
    String middleName,

    @NotBlank(message = "Last name is required")
    @Size
    (
        max = AppConstants.DTO.EventHosts.Sizes.MAX_NAME_LENGTH,
        message = "Last name must not exceed " + AppConstants.DTO.EventHosts.Sizes.MAX_NAME_LENGTH + " characters"
    )
    String lastName
)
{
}
