package com.ticketproject.webapp.dtos.requests;

import com.ticketproject.webapp.constants.AppConstants;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CreatePrivateEventInvitationRequest specifies what a valid request
 * to create an invitation for a private event should look like.
 * 
 * @param publicId the event's public id
 * @param firstName the attendee's first name
 * @param middleName the attendee's middle name (optional)
 * @param lastName the attendee's last name
 * @param email the attendee's email address
 */
@Schema(description = "Request body for creating an invitation to a private event")
public record CreatePrivateEventInvitationRequest
(
    @Schema
    (
        description = "The public ID of the event to register for",
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
        description = "The attendee's first name",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "Jane",
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
        description = "The attendee's middle name (optional)",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        example = "Marie",
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
        description = "The attendee's last name",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "Smith",
        maximum = "100"
    )
    @Size
    (
        max = AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH,
        message = "Last name must not exceed " + AppConstants.Database.EventHosts.Sizes.MAX_NAME_LENGTH + " characters"
    )
    @NotBlank(message = "Last name is required")
    String lastName,

    @Schema
    (
        description = "The attendee's email address, where the ticket will be sent",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "jane.smith@example.com",
        maximum = "254"
    )
    @NotBlank(message = "Email address is required")
    @Email
    (
        // Without this regular expression, the @Email annotation will accept
        // email addresses without a top-level domain, which isn't want I want
        // because I can't send emails to addresses without a top-level domain.
        regexp = AppConstants.Database.EventHosts.Definitions.EMAIL_ADDRESS_REGEX,
        message = "Must be a valid email address"
    )
    @Size
    (
        max = AppConstants.Database.EventHosts.Sizes.MAX_EMAIL_LENGTH,
        message = "Email address must not exceed " + AppConstants.Database.EventHosts.Sizes.MAX_EMAIL_LENGTH + " characters"
    )
    String email
)
{
}
