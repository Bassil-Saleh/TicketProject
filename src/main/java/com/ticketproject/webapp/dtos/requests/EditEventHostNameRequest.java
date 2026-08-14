package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Request body for editing the authenticated event host's name")
public record EditEventHostNameRequest
(
    @Schema
    (
        description = "The event host's first name",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "John",
        maximum = "100"
    )
    @NotBlank(message = "First name is required")
    @Size
    (
        max = AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH,
        message = "First name must not exceed " + AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH + " characters"
    )
    String firstName,

    @Schema
    (
        description = "The event host's middle name (optional)",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        example = "Michael",
        maximum = "100"
    )
    @Size
    (
        max = AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH,
        message = "Middle name must not exceed " + AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH + " characters"
    )
    String middleName,

    @Schema
    (
        description = "The event host's last name",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "Doe",
        maximum = "100"
    )
    @NotBlank(message = "Last name is required")
    @Size
    (
        max = AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH,
        message = "Last name must not exceed " + AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH + " characters"
    )
    String lastName
)
{
}